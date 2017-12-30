Ajapaik API v1.0
=======

Every API call returns a JSON response.

# Data Types

	INTEGER - An integer value such as '2' or '50'
	NUMBER - A number value such as '2' or '5.3'
	STRING - A string value

# Error codes

Error responses are standardised - very API call include an error key at the root level:

	{ "error": 1000 }

List of possible error codes:

	[standard]
    0 - no error (default)
    1 - unknown error
    2 - invalid input parameter
    3 - missing input parameter
    4 - access denied
    5 - session is required
    6 - session is expired
    7 - session is invalid
    8 - user exists in the DB already
    9 - application version is not supported
    10 - user does not exist
    11 - wrong password for existing user

# Hyperlinks

Hyperlinks support 2 different notation styles. Either it's a simple string or a dictionary with a name and url:

	"source1": "http://example.com",
	"source2": {
		"url": "http://example.com",
		"name": "Example 1"
	}

# Authentication

All the API calls require a valid session, but accounts are created automatically so it's invisible to end-users.

Session parameters are standardised:

	[session]:
	STRING _u [R] = user ID (required)
	STRING _s [R] = session ID (required)
	STRING _v [O] = client version ID (optional)
	STRING _l [O] = client language code (optional)

## Login

Performs a login or creates an account if possible (type=auto). Returns a new session.

	/login
    
    Parameters:
        STRING type [R] - Login type. Use 'auto' to automatically create an account
        STRING username [R] - Username or a randomly generated unique identifier
        STRING password [R] - Hashed password or a randomly generated password
        NUMBER version [O] - API version ID
        INTEGER length [O=0] - Session length in seconds. 0 - automatic
        STRING os [O=android] - Platform
    
    Notes:
    	If the type is 'google', then the email is username and google auth token is password.
    	If the type is 'fb', then the FB user ID is username and FB token is password.
    	If the type is 'ajapaik' then it's a normal Ajapaik account
    
    Returns:
        {
        	"error": 0,
        	"id": 1234, /* _u value for the subsequent API calls */
        	"session": "12345678", /* _s value for the subsequent API calls */
        	"expires": 1000 /* Hint in seconds when the session should be automatically cleared by the client. */
        }
    
    Errors:
        [standard]

## Logout

Performs a logout. The session is not valid after this call.

	/logout
    
    Parameters:
        [session]
    
    Returns:
        { "error": 0 }
    
    Errors:
        [standard]

## Register

Registers a new account and allows to convert automatic accounts into regular, FB or Google ones if the session is available.

	/register
	
	Parameters:
		[session is optional]
		STRING type [R] - Login type. See /login
		STRING username [R] - Username
        STRING password [R] - Password
        INTEGER length [O=0] - Session length in seconds. 0 - automatic
        STRING os [O=android] - Platform
        STRING firstname [R] - Firstname
        STRING lastname [R] - Lastname
    
    Returns:
        See /login
    
    Errors:
        [standard]

# Album calls

## Albums

Returns all the albums.

	/albums
	
	Parameters:
	    [session]
	
	Returns:
	    {
	        "error": 0,
	        "albums": [
	            {
	                "id": 1234, /* Album ID */
	                "title": "Abc" /* Album name */,
	                "image": "http://www.example.org/image.png", /* Album thumbnail image */
	                "stats" {
	                	"rephotod": 0, /* Number of photos that have rephotos in the album */
	                	"total": 0 /* Number of photos in the album */
	                }
	            }
	        ]
	    }
	
	Errors:
	    [standard]

## Nearest

Returns a dynamically generated album with the nearest photos to the specified coordinate

    /album/nearest
    
    Parameters:
        [session]
        NUMBER latitude [R] - User coordinate (latitude)
        NUMBER longitude [R] - User coordinate (longitude)
        NUMBER range [O] - Range hint in metres
        STRING state [O] - The value of the state parameter from the previous API call
    
    Returns:
        {
        	"error": 0,
        	"state": "ABCDEF012345789", /* State variable managed by the back-end that can be used to track client-app state */
	        "title": "Abc" /* Album name if available */,
	        "photos": [
	        	{
	        		"id": 1234, /* Photo ID */
	        		"distance": 123.434, /* Distance in meters from the coordinate */
	        		"image": "http://www.example.org/image.png", /* Photo image */
	        		"width": 123, /* Image width in pixels, used to layout */
	        		"height": 123, /* Image height in pixels, used to layout */
	        		"title": "Title", /* Photo title */
	        		"date": "09-12-2010", /* Photo date if available */
	        		"author": "Author Name", /* Photo author if available */
	        		"source": "http://example.com", /* Photo source. See: hyperlinks */
	        		"latitude": 59.4276657, /* Photo coordinate (latitude) */
	        		"longitude": 24.7307741, /* Photo coordinate (longitude) */
	        		"rephotos": 2, /* Number of rephotos */
	        		"uploads": 0 /* Number of my uploads */
	        		"favorited": true /* true if user has favorited this photo */
	        	}
	        ],
	        "photos+": [
	        	/* Optional, photos to add or update */
	        ],
	        "photos-": [
	        	1234, 1235, 1236 /* Optional, photos to remove
	        ]
        }
    
    Errors:
        [standard]

# Photo calls

## Photo state

Returns the current state for a photo.

    /photo/state
    
    Parameters:
        [session]
        INTEGER id [R] - Photo ID
    
    Returns:
        {
			"id": 1234, /* Photo ID */
			"distance": 123.434, /* Distance in meters from the coordinate */
			"image": "http://www.example.org/image.png", /* Photo image */
			"width": 123, /* Image width in pixels, used to layout */
			"height": 123, /* Image height in pixels, used to layout */
			"title": "Title", /* Photo title */
			"date": "09-12-2010", /* Photo date if available */
			"author": "Author Name", /* Photo author if available */
			"source": "http://example.com", /* Photo source. See: hyperlinks */
			"latitude": 59.4276657, /* Photo coordinate (latitude) */
			"longitude": 24.7307741, /* Photo coordinate (longitude) */
			"rephotos": 2, /* Number of rephotos */
			"uploads": 0 /* Number of my uploads */
		}
    
    Errors:
        [standard]

## Photo favorite status update

Updates photo favorited status

    /photo/favorite

    Parameters:
        [session]
        INTEGER id [R] - Photo ID
        STRING favorited [R] - "true" if new status is favorited

    Returns:
        { "error": 0 }

    Errors:
        [standard]

## Photo upload

Uploads a new photo (re-photo). The request is in MULTIPART encoding that includes a JPEG image named 'original'.

    /photo/upload
    
    Parameters:
        [session]
        INTEGER id [R] - Photo ID
        NUMBER latitude [O] - Photo coordinate (latitude)
        NUMBER longitude [O] - Photo coordinate (longitude)
        NUMBER accuracy [O] - Photo coordinate accuracy in metres
        NUMBER age [O] - Photo coordinate age in seconds
        STRING date [R] - Photo date ('30-12-2014')
        NUMBER scale [R] - Photo scale factor
    	NUMBER yaw [R] - Device orientation (yaw)
        NUMBER pitch [R] - Device orientation (pitch)
        NUMBER roll [R] - Device orientation (roll)
        INTEGER flip [R] - Is the photo flipped or not? (1 or 0)
        
    Returns:
        { "error": 0 }
    
    Errors:
        [standard]

## Album state

Returns the current state for an album

    /album/state
    
    Parameters:
        [session]
        INTEGER id [R] - Album ID
        STRING state [O] - The value of the state parameter from the previous API call
    
    Returns:
        See /album/nearest
    
    Errors:
        [standard]

## Favorites

Returns user's favorited photos

    /album/favorites

    Parameters:
        [session]

    Returns:
        See /album/nearest

    Errors:
        [standard]

# User calls

## Profile info

	/user/me
	
	Parameters:
		[session]
		STRING state [O] - The value of the state parameter from the previous API call
	
	Returns:
		{
			"error": 0,
			"state": "ABCDEF012345789", /* State variable managed by the back-end that can be used to track client-app state */
			"name": "User Name", /* Full user name if available */
			"avatar": "http://some_avatar_url_here", /* Avatar if available */
			"rephotos": 123 /* Number of rephotos */
			"rank": 0, /* User rank in the overall leaderboard, 0 if not applicable */
			"message": "Message", /* An optional message from the server to display */,
			"link": "http://", /* An optional link to open in the browser. See: hyperlinks */
		}
	
	Errors:
        [standard]

## Register for push notifications

Tells the back-end to send push notifications.

	/user/device/register
	
	Parameters:
		[session]
		STRING id [R] - Device ID
		STRING type [R] - Push notification service type (Google - 'gcm', Apple - 'apns')
		STRING token [R] - The token from the push notification service
		STRING filter [O=any] - Comma-separated list of notification types to send
	
	Returns:
        { "error": 0 }
    
    Errors:
        [standard]

## Unregister for push notifications

Tells the back-end not to send any additional push notifications.

	/user/device/unregister
	
	Parameters:
		[session]
		STRING id [R] - Device ID
		STRING type [R] - Push notification service type (Google - 'gcm', Apple - 'apns')
		STRING token [R] - The token from the push notification service
	
	Returns:
        { "error": 0 }
    
    Errors:
        [standard]
