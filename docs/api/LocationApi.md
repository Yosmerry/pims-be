# Location API

## Common Request Headers

```http
Authorization: Bearer <access-token>
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
```

## Create Location

- Endpoint: `/api/v1/locations`
- Method: `POST`

### Request Body

```json
{
  "name": "Garage",
  "description": "Tools and items stored in the garage"
}
```

### Response Success

- HTTP status: `201 Created`

```json
{
  "code": 201,
  "data": {
    "code": "LOC000001",
    "name": "Garage",
    "description": "Tools and items stored in the garage",
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

## Get Locations

- Endpoint: `/api/v1/locations`
- Method: `GET`
- Result order: location name ascending

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": [
    {
      "code": "LOC000001",
      "name": "Garage",
      "description": "Tools and items stored in the garage",
      "status": "ACTIVE",
      "createdDate": 1784250000000,
      "updatedDate": 1784250000000
    }
  ],
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response Error

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

## Get Location Detail

- Endpoint: `/api/v1/locations/{code}`
- Method: `GET`

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": {
    "code": "LOC000001",
    "name": "Garage",
    "description": "Tools and items stored in the garage",
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

#### Location Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "location": [
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

## Update Location

- Endpoint: `/api/v1/locations/{code}`
- Method: `PUT`

### Request Body

```json
{
  "name": "Storage Room",
  "description": "Storage boxes and household supplies",
  "status": "ACTIVE"
}
```

### Response Success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": {
    "code": "LOC000001",
    "name": "Storage Room",
    "description": "Storage boxes and household supplies",
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

#### Location Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "location": [
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

## Delete Location

- Endpoint: `/api/v1/locations/{code}`
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

#### Location Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "location": [
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
