package name.jurgenei.example.lineage;

import org.springframework.stereotype.Service;

@Service
public class LineageLoaderService {

    private final LineageLoaderRepository repository;

    public LineageLoaderService(LineageLoaderRepository repository) {
        this.repository = repository;
    }

    public void runLoad() {
        repository.loadCustomerLineage();
    }
}
