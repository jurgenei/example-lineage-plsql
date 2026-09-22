#!/usr/bin/env python3
import argparse
import datetime
import sys
from pypdf import PdfReader, PdfWriter, PageObject
from pypdf.generic import RectangleObject, NameObject, DictionaryObject, ArrayObject, FloatObject

# Paper sizes in points (1 pt = 1/72 inch)
SIZES = {
    "a0": (2383.94, 3370.39),
    "a1": (1683.78, 2383.94),
    "a2": (1190.55, 1683.78),
    "a3": (841.89, 1190.55),
    "a4": (595.28, 841.89),
}

def draw_border_and_title(page, filename, author, border, title):
    """Embed border and title block using page content stream manipulation."""
    from pypdf.generic import DecodedStreamObject, NameObject

    border_right = page.mediabox.width-20
    border_top = page.mediabox.height-20

    content = b""
    if border:
        content += b"0 w 0 0 0 RG\n"  # black line
        content += b"10 10 " + f"{border_right} {border_top}".encode() + b" re S\n"

    if title:
        # Title block area: bottom right rectangle of fixed height
        block_height = 50
        block_width  = 300 # page.mediabox.width * 0.6
        block_left = border_right - block_width + 10
        content += f"1 w 0 0 0 rg {block_left} 10 {block_width} {block_height} re S\n".encode()

        fname = filename.replace(".pdf", "").replace("_"," ")
        today = datetime.date.today().strftime("%Y-%m-%d")

        # Add text as raw PDF operators
        text = (
                f"BT /F1 16 Tf {block_left + 10} 40 Td ({fname}) Tj ET\n"
                f"BT /F1 10 Tf {block_left + 10} 20 Td ({today}) Tj ET\n"
                f"BT /F1 10 Tf {block_left + 70} 20 Td ({author}) Tj ET\n"
        )
        content += text.encode("latin1")

    if content:
        new_stream = DecodedStreamObject()
        new_stream.set_data(content)
        new_resources = DictionaryObject({
            NameObject("/Font"): DictionaryObject({
                NameObject("/F1"): DictionaryObject({
                    NameObject("/Type"): NameObject("/Font"),
                    NameObject("/Subtype"): NameObject("/Helvetica"),
                    NameObject("/BaseFont"): NameObject("/Helvetica"),
                })
            })
        })
        page[NameObject("/Resources")] = new_resources
        page[NameObject("/Contents")] = new_stream

def scale_and_center_page(new_page, src_page, target_size, with_title, border):
    """Scale and center src_page into a new page of target_size, respecting title block."""
    # from pypdf import PageObject

    target_w, target_h = target_size
    src_w = float(src_page.mediabox.width)
    src_h = float(src_page.mediabox.height)

    # Determine orientation
    if src_w > src_h:
        target_w, target_h = target_h, target_w

    # new_page = PageObject.create_blank_page(width=target_w, height=target_h)

    # account for border and title block
    margin = 20 if border else 0
    block_height = 50 if with_title else 0

    scale_x = (target_w - 2 * margin) / src_w
    scale_y = (target_h - 2 * margin - block_height) / src_h
    scale = min(scale_x, scale_y)

    tx = (target_w - src_w * scale) / 2
    ty = (target_h - src_h * scale - block_height) / 2 + block_height

    new_page.merge_transformed_page(src_page, (scale, 0, 0, scale, tx, ty))
    return new_page

def main():
    print("INFO: args = ", sys.argv)
    parser = argparse.ArgumentParser(description="Concatenate and decorate PDF pages using pypdf.")
    parser.add_argument("output", help="Output PDF file")
    parser.add_argument("inputs", nargs="+", help="Input one-page PDF files")
    parser.add_argument("--size", choices=["a0","a1","a2","a3","a4"], default="a4", help="Output size (default a4)")
    parser.add_argument("--author", default="Jurgen Hildebrand <Jurgen.Hildebrand@ing.com>", help="Author name")
    parser.add_argument("--no-border", action="store_true", help="Omit border")
    parser.add_argument("--no-title", action="store_true", help="Omit title block")

    args = parser.parse_args()

    writer = PdfWriter()
    page_size = SIZES[args.size.lower()]

    for infile in sorted(args.inputs):
        reader = PdfReader(infile)
        src_page = reader.pages[0]

        target_w, target_h = page_size
        src_w = float(src_page.mediabox.width)
        src_h = float(src_page.mediabox.height)
        # Determine orientation
        if src_w > src_h:
            target_w, target_h = target_h, target_w

        new_page = PageObject.create_blank_page(width=target_w, height=target_h)
        draw_border_and_title(
            new_page,
            filename=infile.split("/")[-1],
            author=args.author,
            border=not args.no_border,
            title=not args.no_title
        )
        scale_and_center_page(new_page,src_page, page_size, not args.no_title, not args.no_border)
        writer.add_page(new_page)

    writer.compress_identical_objects()
    with open(args.output, "wb") as f_out:
        writer.write(f_out)

if __name__ == "__main__":
    main()
