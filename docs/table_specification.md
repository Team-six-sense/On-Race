# Table Specification

This document provides a detailed specification of the database tables used in the On-Race project, based on JPA entity definitions.

## Auth Domain

### 1. users
Information about registered users.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| email | VARCHAR(100) | No | User email (Login ID) |
| name | VARCHAR(50) | No | User name |
| phone_number | VARCHAR(20) | Yes | Phone number |
| mobile | VARCHAR(20) | Yes | Mobile number |
| password | VARCHAR(255) | Yes | Encrypted password (null for OAuth) |
| auth_provider | VARCHAR(20) | No | Auth provider (LOCAL, KAKAO, etc.) |
| provider_id | VARCHAR(100) | Yes | OAuth provider unique ID |
| role | VARCHAR(20) | No | User role (USER, ADMIN) |
| status | VARCHAR(20) | No | User status (ACTIVE, INACTIVE) |
| verification_status | VARCHAR(20) | No | Email verification status |
| marketing_consent | TINYINT(1) | No | Marketing consent flag |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 2. term_master
Master information for various terms and conditions.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| name | VARCHAR(100) | No | Term name |
| required | TINYINT(1) | No | Required flag |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 3. term_version
Version history for terms and conditions.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| term_master_id | BIGINT | No | Foreign key to term_master |
| version | VARCHAR(10) | No | Version string |
| content | TEXT | Yes | Term content |
| active | TINYINT(1) | No | Currently active flag |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 4. term_user
User agreement history for terms.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| user_id | BIGINT | No | User ID |
| term_version_id | BIGINT | No | Foreign key to term_version |
| agreed | TINYINT(1) | No | Agreement flag |
| agreed_at | DATETIME | No | Agreement time |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 5. login_histories
History of user logins.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| user_id | BIGINT | Yes | User ID |
| login_ip | VARCHAR(45) | Yes | Login IP address |
| login_agent | VARCHAR(512) | Yes | User agent string |
| login_method | VARCHAR(20) | No | Login method |
| auth_provider | VARCHAR(20) | No | Auth provider used |
| is_success | TINYINT(1) | No | Success flag |
| fail_reason | VARCHAR(255) | Yes | Failure reason if not successful |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 6. email_sends
History of emails sent to users.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| user_id | BIGINT | Yes | User ID |
| email | VARCHAR(100) | No | Recipient email |
| type | VARCHAR(20) | No | Type of email sent |
| is_success | TINYINT(1) | No | Success flag |
| fail_reason | VARCHAR(255) | Yes | Failure reason if not successful |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

## Event Domain

### 7. event
Main event information.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| title | VARCHAR(50) | No | Event title |
| type | VARCHAR(20) | No | Event type |
| app_type | VARCHAR(20) | No | Application type (FIRST_COME, LOTTERY) |
| event_at | DATETIME | No | Event date and time |
| app_start_at | DATETIME | No | Application start time |
| app_end_at | DATETIME | No | Application end time |
| region | VARCHAR(20) | No | Event region |
| venue | VARCHAR(255) | No | Event venue |
| lottery_announced_at| DATETIME | Yes | Lottery results announcement time |
| discount_rate | DECIMAL | Yes | Discount rate |
| notice | TEXT | Yes | Event notice |
| is_view | TINYINT(1) | No | Visibility flag |
| is_deleted | TINYINT(1) | No | Soft delete flag |
| sold_out | TINYINT(1) | No | Sold out flag |
| is_queue | TINYINT(1) | No | Waitlist/Queue enabled flag |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 8. event_item
Items provided or sold during events.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| name | VARCHAR(50) | No | Item name |
| price | BIGINT | No | Item price |
| description | VARCHAR(500) | Yes | Item description |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 9. event_item_option
Options for event items (e.g., sizes).

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| item_id | BIGINT | No | Foreign key to event_item |
| option | VARCHAR(20) | No | Option name/value |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 10. event_course
Courses available in an event (e.g., 5km, 10km).

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| event_id | BIGINT | No | Foreign key to event |
| name | VARCHAR(50) | No | Course name |
| map_url | VARCHAR(500) | Yes | URL for course map |
| distance_meter | INT | No | Distance in meters |
| time_limit | INT | No | Time limit in minutes |
| water_source | INT | No | Number of water stations |
| altitude | INT | No | Cumulative altitude |
| course_route | VARCHAR(500) | No | Course route description |
| price | BIGINT | No | Course registration price |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 11. event_pace
Pace options for a course (e.g., Group A, Group B).

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| course_id | BIGINT | No | Foreign key to event_course |
| name | VARCHAR(50) | No | Pace group name |
| hour | INT | No | Target hour |
| minutes | INT | No | Target minutes |
| capacity | INT | No | Max capacity for this pace |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 12. event_stock
Stock management for event pace groups.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| event_pace_id | BIGINT | No | Foreign key to event_pace (Unique) |
| total_stock | INT | No | Total available spots |
| confirmed_stock | INT | No | Confirmed registrations |
| version | BIGINT | Yes | Optimistic locking version |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 13. event_sales_info
Detailed sales and refund policies for events.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| event_id | BIGINT | No | Foreign key to event (Unique) |
| is_refundable | TINYINT(1) | No | Refundable flag |
| is_transferable | TINYINT(1) | No | Transferable flag |
| refund_start_at | DATETIME | Yes | Refund start date |
| refund_end_at | DATETIME | Yes | Refund end date |
| non_refundable_at | DATETIME | Yes | Date after which no refund |
| cancellation_fee | VARCHAR(255) | Yes | Cancellation fee rules |
| refund_policy | TEXT | Yes | Refund policy text |
| weather_refund | TEXT | Yes | Weather-related refund policy |
| delivery_target | VARCHAR(100) | Yes | Delivery target description |
| delivery_method | VARCHAR(50) | Yes | Delivery method |
| delivery_start_at | DATETIME | Yes | Delivery start date |
| delivery_end_at | DATETIME | Yes | Delivery end date |
| delivery_fee | VARCHAR(50) | Yes | Delivery fee description |
| delivery_area | VARCHAR(200) | Yes | Delivery area |
| delivery_info | VARCHAR(200) | Yes | Additional delivery info |
| address_change_period| DATETIME| Yes | Address change deadline |
| delivery_compensation| VARCHAR(500)| Yes| Non-delivery compensation |
| seller_name | VARCHAR(100) | Yes | Seller name |
| business_no | VARCHAR(20) | Yes | Seller business registration number |
| ecommerce_no | VARCHAR(50) | Yes | E-commerce report number |
| is_ecommerce_mediator| TINYINT(1) | No | E-commerce mediator flag |
| customer_service | VARCHAR(50) | Yes | CS contact number |
| seller_address | VARCHAR(200) | Yes | Seller business address |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 14. event_package
Mapping between events and standard items.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| event_id | BIGINT | No | Foreign key to event |
| item_id | BIGINT | No | Foreign key to event_item |
| item_type | VARCHAR(20) | No | Item type |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 15. event_image
Images associated with an event.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| event_id | BIGINT | No | Foreign key to event |
| type | VARCHAR(20) | No | Image type (THUMBNAIL, MAIN, etc.) |
| url | VARCHAR(500) | No | Image URL |
| sort | INT | No | Sorting order |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

## Order & Entry Domain

### 16. orders
Information about event registrations and payments.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| order_number | VARCHAR(255) | No | Unique order number |
| user_id | BIGINT | No | User ID |
| event_course_id | BIGINT | No | Course ID |
| event_pace_id | BIGINT | Yes | Pace group ID |
| entry_id | BIGINT | Yes | Entry ID |
| event_id | BIGINT | Yes | Event ID (Snapshot) |
| event_title | VARCHAR(50) | Yes | Event title (Snapshot) |
| event_app_type | VARCHAR(20) | Yes | Application type (Snapshot) |
| event_status | VARCHAR(20) | Yes | Event status (Snapshot) |
| event_at | DATETIME | Yes | Event date (Snapshot) |
| event_venue | VARCHAR(255) | Yes | Event venue (Snapshot) |
| course_name | VARCHAR(50) | Yes | Course name (Snapshot) |
| pace_name | VARCHAR(50) | Yes | Pace name (Snapshot) |
| order_status | VARCHAR(20) | No | Order status (PENDING, PAID, etc.) |
| item_total_amount | BIGINT | No | Total item price |
| shipping_fee | BIGINT | No | Shipping fee |
| discount_amount | BIGINT | No | Discount amount |
| final_amount | BIGINT | No | Total payment amount |
| recipient_name | VARCHAR(50) | No | Shipping recipient name |
| address_label | VARCHAR(20) | Yes | Address label (Home, Office, etc.) |
| recipient_phone | VARCHAR(20) | No | Recipient phone number |
| zip_code | VARCHAR(10) | No | Shipping zip code |
| address | VARCHAR(200) | No | Shipping address |
| detail_address | VARCHAR(200) | Yes | Detailed address |
| delivery_memo | VARCHAR(200) | Yes | Delivery memo |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 17. entry
User's participation record in a specific event.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| user_id | BIGINT | No | User ID |
| event_id | BIGINT | No | Foreign key to event |
| course_id | BIGINT | No | Foreign key to event_course |
| pace_id | BIGINT | No | Foreign key to event_pace |
| status | VARCHAR(20) | No | Entry status (RESERVED, APPLIED) |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 18. order_package
Specific package items included in an order.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| order_id | BIGINT | No | Foreign key to orders |
| event_package_id | BIGINT | No | Foreign key to event_package |
| price | BIGINT | No | Package price at time of order |
| name | VARCHAR(255) | No | Package name |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

## Member & Utility Domain

### 19. members
Membership profile (linked to users).

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key (Matches User ID) |
| is_deleted | TINYINT(1) | No | Deletion flag |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 20. address
Address book for users.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| user_id | BIGINT | No | User ID |
| label | VARCHAR(20) | Yes | Address label |
| normalized_label | VARCHAR(20) | Yes | Normalized label for uniqueness |
| receiver_name | VARCHAR(50) | No | Receiver name |
| phone | VARCHAR(20) | No | Phone number |
| zipcode | VARCHAR(10) | No | Zip code |
| address1 | VARCHAR(255) | No | Base address |
| address2 | VARCHAR(255) | Yes | Detailed address |
| memo | VARCHAR(255) | Yes | Memo |
| is_default | TINYINT(1) | No | Default address flag |
| is_deleted | TINYINT(1) | No | Soft delete flag |
| deleted_at | DATETIME | Yes | Deletion time |
| active_default_owner_id| BIGINT | Yes | ID for default address uniqueness |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |

### 21. media_objects
Management of uploaded media files.

| Column | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| id | BIGINT | No | Primary Key |
| owner_id | BIGINT | No | Owner ID |
| object_key | VARCHAR(512) | No | S3/Storage object key |
| content_type | VARCHAR(100) | No | MIME type |
| media_status | VARCHAR(20) | Yes | Status (PRESIGNED, UPLOADED, etc.) |
| expires_at | DATETIME | No | Presigned URL expiration time |
| confirmed_at | DATETIME | Yes | Confirmation time |
| created_at | DATETIME | No | Record creation time |
| updated_at | DATETIME | No | Record update time |
