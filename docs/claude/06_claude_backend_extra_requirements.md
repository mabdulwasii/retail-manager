# Phase Extra Requirements
Answer the following questions and prompt me before starting the phase:
- What is the relationship between the tenant and the shop, and why are we extracting the tenant from the header in the internal filter?
- What is the purpose of `TenantAwareRepository`?
- Model the JWT token Principal into a Java class.
- Does this project require a gateway as it is?

### Data Backup
- Implement a data backup stored locally in encrypted form.
- Upload the encrypted data to a remote URL whenever internet connectivity is available.
- Backup must occur at least once a week.
- If backups fail for two consecutive weeks, the system should block operations.
- Backup data must include:
    - Fraud reports
    - Audit logs
    - Sales transaction reports
    - Investment reports
    - Others

- Bootstrap a new model that can decrypt the encrypted backup data and can be deployed independently.

### Inventory and Sales
- Include a **sell operation** where only available products can be sold.
- Product return should be possible only after:
    - The fraud rule has been applied successfully.
    - The system has mitigated the risk of damaged products.
- All payments will be by cash for now.
- Selling will require a **manual trigger** from UI using the API.
- The UI should allow querying the price of products before purchase.
- All Product categories should be filterable.
- Support bulk product upload via CSV file.
- Support bulk product update via CSV file.
- Inventory upload should be possible, and the total costs of procurement are validated against the total uploaded inventory. Failure should result in a rollback of the entire operation.
- Support inventory adjustments (e.g., stock corrections).
- Support inventory deactivation and activation.
- Support inventory stock level alerts (e.g., low stock warnings).
- Support inventory stock history tracking (e.g., stock changes over time).
- Inventory should also be audited.
- Inventory should be filterable by shop.
- Inventory should be filterable by product category.
- Inventory should be filterable by product name.
- Inventory should be filterable by product SKU.
- Inventory should be filterable by product status.
- Inventory should be filterable by product location.
- Inventory should be filterable by product price range.
- Inventory should be tied to investment if investment is present and enabled.
- Fraud detection rules should apply to investments in products as well. This should also include the discount calculation
- Sales projection and intelligent advice on the product to be procured should be available.
- Add other projections and intelligent advice as needed for the Shop owner to make better decisions.

### Return Policy
- Include risk management for expired products or damaged goods.
- Support partial refunds for returned products.
- Update inventory after returns.
- Add audit logs for all return operations.

### Product Enhancements
- Support optional metadata for products, available as search criteria.
- Products should have full CRUD operations.
- The list of products should support many filters for easier retrieval.
- Support complex filters for entities like Sales, Audit, and Fraud.
- A product may include an optional **Location** field, which should also be filterable for identifying where the product is located in a shop.  

### Expenses
- Support expenses by the shop for the procurement of products.
- Expenses should be categorized (e.g., procurement, utilities, rent).
- Expenses should be tracked separately from sales.
- Generate reports showing total expenses over a specified period.
- Expenses should be associated with a specific shop, just like profit and loss.
- Expenses should be audited.

### Docker and Kubernetes
- Implement a Docker image for the backend.
- Implement a Kubernetes deployment for the backend using Helm.
- Put all Helm-related files in the `helm-chart` folder.
- Deploy the application to a Docker Desktop Kubernetes cluster using the Helm chart.

### Code
- Refactor the roles to enums
- End all integration tests with **IT and use **Test for unit tests.
- Add a **Jacoco** report to the build.
- Add a **Sonar** report to the build.
- Refactor all magic numbers and string literals to constants in the project.

### Docker Compose
- Fix Keycloak, Kafka, and backend services in the Docker Compose as they are not working.