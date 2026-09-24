MERGE (customerReference:DataObject {name: 'CUSTOMER_REFERENCE'});
MERGE (rawCustomer:DataObject {name: 'RAW_CUSTOMER'});
MERGE (validCustomer:DataObject {name: 'VALID_CUSTOMER'});
MERGE (mergedCustomer:DataObject {name: 'MERGED_CUSTOMER'});
MATCH (customerReference:DataObject {name: 'CUSTOMER_REFERENCE'})
MATCH (validCustomer:DataObject {name: 'VALID_CUSTOMER'})
MERGE (customerReference)-[:FLOWS_TO]->(validCustomer);
MATCH (rawCustomer:DataObject {name: 'RAW_CUSTOMER'})
MATCH (validCustomer:DataObject {name: 'VALID_CUSTOMER'})
MERGE (rawCustomer)-[:FLOWS_TO]->(validCustomer);
MATCH (validCustomer:DataObject {name: 'VALID_CUSTOMER'})
MATCH (mergedCustomer:DataObject {name: 'MERGED_CUSTOMER'})
MERGE (validCustomer)-[:FLOWS_TO]->(mergedCustomer);
