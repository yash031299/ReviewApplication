# mock_alexa.json Structure Documentation

Each record in `mock_alexa.json` represents a product review. The fields are:

| Field           | Type     | Description                                      | Example Value           |
|-----------------|----------|--------------------------------------------------|------------------------|
| id              | String   | Unique identifier for the review                  | "1"                    |
| review          | String   | Text of the review (can be empty or null)         | "Excellent product!"   |
| author          | String   | Name of the reviewer (can be null)                | "Alice"                |
| review_source   | String   | Source/platform of the review                     | "Amazon"              |
| title           | String   | Title of the review                              | "Great buy"            |
| product_name    | String   | Name of the product being reviewed                | "Echo Dot"             |
| reviewed_date   | String   | Date of the review (format: yyyy-MM-dd)           | "2025-07-30"           |
| rating          | Integer  | Rating score (1-5, can be null for missing)       | 5                      |

## Notes
- Some records have missing or null fields to test edge cases.
- The file contains 10 varied records to cover valid, invalid, and edge scenarios.
