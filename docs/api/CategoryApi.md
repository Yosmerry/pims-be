# Category API

## Common Request Headers

```http
Authorization: Bearer <access-token>
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
```

## Create Category

- Endpoint: `/api/v1/categories`
- Method: `POST`

### Request Body

```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

### Response Success

- HTTP status: `201 Created`

```json
{
  "code": 201,
  "data": {
    "code": "CAT000001",
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "status": "ACTIVE",
    "createdDate": 1784250000000,
    "updatedDate": 1784250000000
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
    "name": [
      "Blank",
      "CharacterMoreThan100"
    ],
    "description": [
      "CharacterMoreThan500"
    ]
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
    "authentication": [
      "UserInactive"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Get Categories

- Endpoint: `/api/v1/categories`
- Method: `GET`
- Result order: category name ascending

### Query Parameters

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `page` | No | `0` | Zero-based page number |
| `size` | No | `20` | Items per page, from 1 to 100 |

Example: `GET /api/v1/categories?page=0&size=20`

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "code": "CAT000001",
        "name": "Electronics",
        "description": "Electronic devices and accessories",
        "status": "ACTIVE",
        "createdDate": 1784250000000,
        "updatedDate": 1784250000000
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
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
    "page": ["Blank", "Minimum0"],
    "size": ["Blank", "Minimum1", "Maximum100"]
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
    "authentication": [
      "UserInactive"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Get Category Detail

- Endpoint: `/api/v1/categories/{code}`
- Method: `GET`

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": {
    "code": "CAT000001",
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "status": "ACTIVE",
    "createdDate": 1784250000000,
    "updatedDate": 1784250000000
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response Error

#### Category Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "category": [
      "NotFound"
    ]
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
    "authentication": [
      "UserInactive"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Update Category

- Endpoint: `/api/v1/categories/{code}`
- Method: `PUT`

### Request Body

```json
{
  "name": "Electronics and Gadgets",
  "description": "Electronic devices, gadgets, and accessories",
  "status": "ACTIVE"
}
```

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": {
    "code": "CAT000001",
    "name": "Electronics and Gadgets",
    "description": "Electronic devices, gadgets, and accessories",
    "status": "ACTIVE",
    "createdDate": 1784250000000,
    "updatedDate": 1784260000000
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
    "name": [
      "Blank",
      "CharacterMoreThan100"
    ],
    "description": [
      "CharacterMoreThan500"
    ],
    "status": [
      "Blank",
      "Invalid"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Category Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "category": [
      "NotFound"
    ]
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
    "authentication": [
      "UserInactive"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Delete Category

- Endpoint: `/api/v1/categories/{code}`
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

#### Category Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "category": [
      "NotFound"
    ]
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
    "authentication": [
      "UserInactive"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```
