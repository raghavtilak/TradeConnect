const jwt = require('jsonwebtoken');
exports.verifyToken = (req, res, next)=>{
    const authHeader = req.headers.authorization;
    if (authHeader) {
      // Split the authorization header into two parts: the scheme and token
      const [scheme, token] = authHeader.split(' ');
  console.log(token);
      if (scheme === 'Bearer' && token) {
        // Verify the token using the secret key
        jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
          if (err) {
            return res.status(403).json({ message: 'Invalid token Please sign in again' });
          }
  
          // Attach the decoded user data to the request object for use in subsequent middleware and route handlers
          req.user = decoded.id;
          console.log(decoded);
          next();
        });
      } else {
        // Return a 401 Unauthorized error if the authorization scheme is not "Bearer" or no token is provided
        res.status(401).json({ message: 'Invalid authorization header' });
      }
    } else {
      // Return a 401 Unauthorized error if no authorization header is provided
      res.status(401).json({ message: 'Authorization header missing' });
    }
}
  