//  Voor JWT - login
//Request-object maken voor de login-body.
package nl.hu.bep.shopping.webservices;

// Deze klasse stelt de JSON-body van de login request voor.
// Bijvoorbeeld:
// {
//   "username": "leroy",
//   "password": "password123"
// }
public class LogonRequest {
    public String username;
    public String password;
}