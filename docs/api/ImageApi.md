# Image API

## Common Request Headers

```http
Authorization: Bearer <access-token>
X-CHANNEL-ID: WEB
X-SERVICE-ID: pims-fe
X-REQUEST-ID: abf25843-07f8-46fe-9823-0a6e4fd7499f
```

## Upload Inventory Item Image

- Endpoint: `/api/v1/inventory-items/{inventoryItemCode}/images`
- Method: `POST`
- Content type: `multipart/form-data`
- Allowed files: JPEG and PNG
- Maximum file size: 5 MB
- Behavior: the first image uploaded for an item is marked as primary

### Request Part

| Part | Type | Required | Description |
| --- | --- | --- | --- |
| `file` | Binary | Yes | JPEG or PNG image, maximum 5 MB |

Example:

```bash
curl --request POST \
  --url http://localhost:8080/api/v1/inventory-items/ITM000001/images \
  --header 'Authorization: Bearer <access-token>' \
  --header 'X-CHANNEL-ID: WEB' \
  --header 'X-SERVICE-ID: pims-fe' \
  --form 'file=@laptop.png'
```

### Response Success

- HTTP status: `201 Created`

```json
{
  "code": 201,
  "data": {
    "code": "IMG000001",
    "inventoryItemCode": "ITM000001",
    "originalFilename": "laptop.png",
    "contentType": "image/png",
    "fileSize": 245760,
    "primary": true,
    "url": "/api/v1/images/IMG000001",
    "createdDate": 1784509200000
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

### Response Error

#### Validation Error

- HTTP status: `400 Bad Request`

Only the applicable error code is returned.

```json
{
  "code": 400,
  "errors": {
    "file": [
      "Blank",
      "FileTooLarge",
      "UnsupportedFileType",
      "Invalid",
      "CharacterMoreThan255"
    ]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

#### Inventory Item Not Found

- HTTP status: `404 Not Found`
- Returned when the item does not exist or is not owned by the current user

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

#### Storage Failure

- HTTP status: `500 Internal Server Error`

```json
{
  "code": 500,
  "errors": {
    "file": ["StorageFailed"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Get Image Content

- Endpoint: `/api/v1/images/{imageCode}`
- Method: `GET`
- Response content type: `image/jpeg` or `image/png`
- Response body: binary image content

The endpoint requires the same authorization and common headers. It returns the
image only when its inventory item belongs to the current user. Vue can request
the response as a blob and create an object URL for display.

### Response Success

- HTTP status: `200 OK`
- Headers include `Content-Type`, `Content-Length`, and inline
  `Content-Disposition`

### Response Error

#### Image Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "image": ["NotFound"]
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

#### Storage Failure

- HTTP status: `500 Internal Server Error`

```json
{
  "code": 500,
  "errors": {
    "file": ["StorageFailed"]
  },
  "metadata": {
    "requestId": "abf25843-07f8-46fe-9823-0a6e4fd7499f"
  }
}
```

## Delete Image

- Endpoint: `/api/v1/images/{imageCode}`
- Method: `DELETE`
- Behavior: soft-delete the image record and remove the stored file after the
  database transaction commits
- Primary behavior: when the deleted image is primary, the oldest remaining
  image becomes primary

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

#### Image Not Found

- HTTP status: `404 Not Found`

```json
{
  "code": 404,
  "errors": {
    "image": ["NotFound"]
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

## Storage Configuration

```yaml
pims:
  image:
    storage-directory: ${IMAGE_STORAGE_DIRECTORY:uploads/images}
    max-file-size: ${IMAGE_MAX_FILE_SIZE:5242880}
```

The default local directory is `uploads/images`. Files are stored below a folder
named with their inventory item code. Runtime uploads are excluded from Git.
