# Inventory API

## Common Request Headers

```http
Authorization: Bearer <access-token>
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
```

## Values

- Condition: `NEW`, `GOOD`, `FAIR`, `POOR`, `DAMAGED`
- Status: `OWNED`, `LOANED`, `SOLD`, `LOST`, `DISPOSED`
- Sort: `name:asc`, `name:desc`, `updatedDate:asc`,
  `updatedDate:desc`, `purchaseDate:asc`, or `purchaseDate:desc`

## Create Inventory Item

- Endpoint: `/api/v1/inventory-items`
- Method: `POST`
- Default status: `OWNED`

### Request Body

```json
{
  "categoryCode": "CAT000001",
  "locationCode": "LOC000001",
  "name": "MacBook Pro",
  "description": "Work laptop",
  "quantity": 1,
  "purchasePrice": 25000000.00,
  "purchaseDate": "2026-07-01",
  "condition": "NEW",
  "notes": "Includes charger"
}
```

`locationCode`, `description`, `purchasePrice`, `purchaseDate`, and `notes`
are optional. `categoryCode` must identify an active category owned by the current
user. When provided, `locationCode` must identify an active location owned by the
current user.

### Response Success

- HTTP status: `201 Created`

```json
{
  "code": 201,
  "data": {
    "code": "ITM000001",
    "categoryCode": "CAT000001",
    "locationCode": "LOC000001",
    "name": "MacBook Pro",
    "description": "Work laptop",
    "quantity": 1,
    "purchasePrice": 25000000.00,
    "purchaseDate": "2026-07-01",
    "condition": "NEW",
    "status": "OWNED",
    "notes": "Includes charger",
    "createdDate": 1784422800000,
    "updatedDate": 1784422800000
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response Error

#### Validation Error

- HTTP status: `400 Bad Request`

```json
{
  "code": 400,
  "errors": {
    "categoryCode": ["Blank", "CharacterMoreThan20", "Invalid"],
    "locationCode": ["CharacterMoreThan20", "Invalid"],
    "name": ["Blank", "CharacterMoreThan150"],
    "description": ["CharacterMoreThan1000"],
    "quantity": ["Blank", "Minimum1"],
    "purchasePrice": ["Minimum0", "InvalidFormat"],
    "purchaseDate": ["FutureDate"],
    "condition": ["Blank", "Invalid"],
    "notes": ["CharacterMoreThan1000"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Unauthorized

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Inactive User

- HTTP status: `403 Forbidden`

```json
{
  "code": 403,
  "errors": {
    "authentication": ["UserInactive"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Get Inventory Items

- Endpoint: `/api/v1/inventory-items`
- Method: `GET`
- Default sort: `updatedDate:desc`

### Query Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `search` | No | Case-insensitive search in name, description, and notes |
| `categoryCode` | No | Filter by category code |
| `locationCode` | No | Filter by location code |
| `condition` | No | Filter by an allowed condition |
| `status` | No | Filter by an allowed status |
| `sortBy` | No | Sort using one of the allowed sort values |

Example:

```http
GET /api/v1/inventory-items?search=macbook&condition=GOOD&sortBy=name:asc
```

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": [
    {
      "code": "ITM000001",
      "categoryCode": "CAT000001",
      "locationCode": "LOC000001",
      "name": "MacBook Pro",
      "description": "Work laptop",
      "quantity": 1,
      "purchasePrice": 25000000.00,
      "purchaseDate": "2026-07-01",
      "condition": "GOOD",
      "status": "OWNED",
      "notes": "Includes charger",
      "createdDate": 1784422800000,
      "updatedDate": 1784509200000
    }
  ],
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response Error

#### Validation Error

- HTTP status: `400 Bad Request`

```json
{
  "code": 400,
  "errors": {
    "search": ["CharacterMoreThan150"],
    "categoryCode": ["CharacterMoreThan20"],
    "locationCode": ["CharacterMoreThan20"],
    "condition": ["Invalid"],
    "status": ["Invalid"],
    "sortBy": ["Invalid"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Unauthorized

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Inactive User

- HTTP status: `403 Forbidden`

```json
{
  "code": 403,
  "errors": {
    "authentication": ["UserInactive"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Get Inventory Item Detail

- Endpoint: `/api/v1/inventory-items/{code}`
- Method: `GET`

### Response Success

- HTTP status: `200 OK`

The response `data` uses the same inventory item structure as the create
response.

### Response Error

#### Inventory Item Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "inventoryItem": ["NotFound"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Unauthorized

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Inactive User

- HTTP status: `403 Forbidden`

```json
{
  "code": 403,
  "errors": {
    "authentication": ["UserInactive"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Update Inventory Item

- Endpoint: `/api/v1/inventory-items/{code}`
- Method: `PUT`

### Request Body

```json
{
  "categoryCode": "CAT000001",
  "locationCode": null,
  "name": "MacBook Pro M3",
  "description": "Work laptop",
  "quantity": 1,
  "purchasePrice": 23000000.00,
  "purchaseDate": "2026-07-01",
  "condition": "GOOD",
  "status": "SOLD",
  "notes": null
}
```

### Response Success

- HTTP status: `200 OK`

The response `data` uses the same inventory item structure as the create
response and contains the updated values.

### Response Error

#### Validation Error

- HTTP status: `400 Bad Request`

The validation fields are the same as create, with the additional required
`status` field. An invalid or inactive owned relation returns `Invalid` for
`categoryCode` or `locationCode`.

#### Inventory Item Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "inventoryItem": ["NotFound"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Unauthorized

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Inactive User

- HTTP status: `403 Forbidden`

```json
{
  "code": 403,
  "errors": {
    "authentication": ["UserInactive"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Delete Inventory Item

- Endpoint: `/api/v1/inventory-items/{code}`
- Method: `DELETE`
- Behavior: soft delete

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": null,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response Error

#### Inventory Item Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "inventoryItem": ["NotFound"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Unauthorized

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Inactive User

- HTTP status: `403 Forbidden`

```json
{
  "code": 403,
  "errors": {
    "authentication": ["UserInactive"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```
