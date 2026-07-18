# Authentication API

## Register User

- Endpoint: `/api/v1/auth/register`
- Method: `POST`
- Authentication: Not required

### Request headers

```http
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
Content-Type: application/json
```

### Request body

```json
{
  "name": "Yos Merry",
  "email": "yos@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

### Response success

- HTTP status: `201 Created`

```json
{
  "code": 201,
  "data": {
    "code": "USR000001",
    "name": "Yos Merry",
    "email": "yos@example.com",
    "status": "ACTIVE",
    "createdDate": 1784250000000,
    "updatedDate": 1784250000000
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response validation error

- HTTP status: `400 Bad Request`

```json
{
  "code": 400,
  "errors": {
    "channelId": [
      "Blank"
    ],
    "serviceId": [
      "Blank"
    ],
    "name": [
      "Blank",
      "CharacterMoreThan150"
    ],
    "email": [
      "Blank",
      "InvalidFormat",
      "CharacterMoreThan255",
      "Duplicate"
    ],
    "password": [
      "Blank",
      "CharacterLessThan8",
      "CharacterMoreThan72",
      "WeakPassword"
    ],
    "confirmPassword": [
      "Blank",
      "PasswordMismatch"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Login

- Endpoint: `/api/v1/auth/login`
- Method: `POST`
- Authentication: Not required

### Request headers

```http
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
Content-Type: application/json
```

### Request body

```json
{
  "email": "yos@example.com",
  "password": "Password123!"
}
```

### Response success

- HTTP status: `200 OK`
- Response cookie: `refresh_token`

```http
Set-Cookie: refresh_token=<refresh-token>; Max-Age=86400; Path=/api/v1/auth; HttpOnly; Secure; SameSite=Strict
```

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "code": "USR000001",
      "name": "Yos Merry",
      "email": "yos@example.com",
      "status": "ACTIVE"
    }
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

`expiresIn` is expressed in seconds.

### Response invalid credentials

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "errors": {
    "authentication": [
      "InvalidCredentials"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

The API returns the same error when the email does not exist or the password is incorrect. This prevents account enumeration.

### Response inactive user

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

### Response validation error

- HTTP status: `400 Bad Request`

```json
{
  "code": 400,
  "errors": {
    "email": [
      "Blank",
      "InvalidFormat"
    ],
    "password": [
      "Blank"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

---

## Refresh Access Token

- Endpoint: `/api/v1/auth/refresh`
- Method: `POST`
- Authentication: Access token not required
- Required cookie: `refresh_token`
- Request body: None

The frontend must send the request with credentials enabled.

```javascript
axios.post('/api/v1/auth/refresh', null, {
  withCredentials: true,
})
```

### Request example

```http
POST /api/v1/auth/refresh HTTP/1.1
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
Cookie: refresh_token=<refresh-token>
```

### Response success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response missing refresh token

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "errors": {
    "refreshToken": [
      "Missing"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response invalid, expired, or revoked refresh token

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "errors": {
    "refreshToken": [
      "Invalid"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Logout

- Endpoint: `/api/v1/auth/logout`
- Method: `POST`
- Authentication: Required
- Required cookie: `refresh_token`
- Request body: None

### Request headers

```http
Authorization: Bearer <access-token>
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
Cookie: refresh_token=<refresh-token>
```

### Response success

- HTTP status: `200 OK`
- The backend sets `revoked_date` for the refresh-token record.
- The backend clears the refresh-token cookie.

```http
Set-Cookie: refresh_token=; Max-Age=0; Path=/api/v1/auth; HttpOnly; Secure; SameSite=Strict
```

```json
{
  "code": 200,
  "data": null,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response unauthorized

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Get Current User

- Endpoint: `/api/v1/auth/me`
- Method: `GET`
- Authentication: Required

### Request headers

```http
Authorization: Bearer <access-token>
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
```

### Response success

- HTTP status: `200 OK`

```json
{
  "code": 200,
  "data": {
    "code": "USR000001",
    "name": "Yos Merry",
    "email": "yos@example.com",
    "status": "ACTIVE",
    "lastLoginDate": 1784250000000,
    "createdDate": 1784240000000,
    "updatedDate": 1784250000000
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response unauthorized

- HTTP status: `401 Unauthorized`

```json
{
  "code": 401,
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response inactive user

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