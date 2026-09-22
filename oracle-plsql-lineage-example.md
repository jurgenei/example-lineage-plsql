# Oracle PL/SQL Data Lineage Example

## Objective

Demonstrate data lineage concepts in a compact Oracle PL/SQL example:

- Direct source-to-target mappings
- Reference data enrichment
- Left outer joins
- Derived columns
- Surrogate key generation
- Foreign key propagation
- Audit columns
- Single stored procedure containing the transformation logic

## Lineage Flow

```text
CUSTOMER_REFERENCE
        |
        | credit rating, risk segment lookup
        v
RAW_CUSTOMER
        |
        | enrichment + derivation
        v
VALID_CUSTOMER
        |
        | downstream propagation
        v
MERGED_CUSTOMER
```

## Source Tables

```sql
create table customer_reference (
    customer_id      varchar2(20) primary key,
    credit_rating    varchar2(10),
    risk_segment     varchar2(20)
);

create table raw_customer (
    customer_id      varchar2(20),
    customer_name    varchar2(100),
    city             varchar2(100),
    annual_revenue   number
);
```

## Target Tables

```sql
create table valid_customer (
    valid_customer_sk number primary key,
    customer_id       varchar2(20),
    customer_name     varchar2(100),
    city              varchar2(100),
    annual_revenue    number,
    credit_rating     varchar2(10),
    risk_segment      varchar2(20),
    customer_status   varchar2(20),
    load_ts           timestamp
);

create table merged_customer (
    merged_customer_sk number primary key,
    valid_customer_sk  number,
    customer_id        varchar2(20),
    customer_name      varchar2(100),
    credit_rating      varchar2(10),
    risk_segment       varchar2(20),
    load_ts            timestamp
);
```

## Sequences

```sql
create sequence valid_customer_seq;
create sequence merged_customer_seq;
```

## Sample Data

```sql
insert into customer_reference values ('C001','AAA','LOW');
insert into customer_reference values ('C002','BBB','MEDIUM');
insert into customer_reference values ('C003','CCC','HIGH');

insert into raw_customer values ('C001','Contoso Ltd','London',1000000);
insert into raw_customer values ('C002','Fabrikam BV','Amsterdam',500000);
insert into raw_customer values ('C999','Unknown Corp','Berlin',250000);
```

## Stored Procedure

```sql
create or replace procedure load_customer_lineage
as
begin

    insert into valid_customer (
        valid_customer_sk,
        customer_id,
        customer_name,
        city,
        annual_revenue,
        credit_rating,
        risk_segment,
        customer_status,
        load_ts
    )
    select
        valid_customer_seq.nextval,
        r.customer_id,
        r.customer_name,
        r.city,
        r.annual_revenue,
        ref.credit_rating,
        ref.risk_segment,
        case
            when ref.customer_id is null then 'UNMATCHED'
            when ref.credit_rating in ('AAA','BBB') then 'ACTIVE'
            else 'REVIEW'
        end,
        systimestamp
    from raw_customer r
    left join customer_reference ref
        on r.customer_id = ref.customer_id;

    insert into merged_customer (
        merged_customer_sk,
        valid_customer_sk,
        customer_id,
        customer_name,
        credit_rating,
        risk_segment,
        load_ts
    )
    select
        merged_customer_seq.nextval,
        vc.valid_customer_sk,
        vc.customer_id,
        vc.customer_name,
        vc.credit_rating,
        vc.risk_segment,
        systimestamp
    from valid_customer vc;

    commit;

end;
/
```

## Column Lineage Highlights

```text
VALID_CUSTOMER.CUSTOMER_ID
    <- RAW_CUSTOMER.CUSTOMER_ID

VALID_CUSTOMER.CREDIT_RATING
    <- CUSTOMER_REFERENCE.CREDIT_RATING

VALID_CUSTOMER.RISK_SEGMENT
    <- CUSTOMER_REFERENCE.RISK_SEGMENT

VALID_CUSTOMER.CUSTOMER_STATUS
    <- CASE(CUSTOMER_REFERENCE.*)

VALID_CUSTOMER.VALID_CUSTOMER_SK
    <- VALID_CUSTOMER_SEQ.NEXTVAL

MERGED_CUSTOMER.VALID_CUSTOMER_SK
    <- VALID_CUSTOMER.VALID_CUSTOMER_SK

MERGED_CUSTOMER.CREDIT_RATING
    <- VALID_CUSTOMER.CREDIT_RATING
```

## Future Enhancements

- Slowly Changing Dimensions (SCD2)
- Multiple source systems
- Data quality rules
- Exception handling tables
- Customer hierarchy lineage
- PL/SQL package-based lineage
