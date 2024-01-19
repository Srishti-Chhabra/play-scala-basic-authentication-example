package services

import javax.inject.Inject

// Dummy User class for illustration
case class User(username: String, password: String)

class AuthService @Inject()() {

  // Dummy list of users (replace with a database or external authentication mechanism)
  private val users: Seq[User] = Seq(
    User("admin", "admin123"),
    User("user", "user123")
  )

  // Authenticate a user based on the provided username and password
  def authenticate(username: String, password: String): Option[User] = {
    users.find(user => (user.username == username && user.password == password))
  }
}