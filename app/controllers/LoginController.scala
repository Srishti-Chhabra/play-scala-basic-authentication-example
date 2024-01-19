package controllers

import javax.inject.Inject
import play.api.mvc._
import play.filters.csrf._
import play.api.data._
import play.api.data.Forms._
import services.{AuthService, User}

class LoginController @Inject()(cc: ControllerComponents, authService: AuthService) extends AbstractController(cc) {

  val formData: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  // Display the login form
  def login: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(""))
  }

  // Handle form submission for authentication
  def authenticate: Action[AnyContent] = Action { implicit request =>
    CSRF.getToken(request).map { _ =>
      try {
        val user: User = formData.bindFromRequest().get
        authService.authenticate(user.username, user.password) match {
          case Some(authenticatedUser) =>
            // Log successful login
            play.Logger.info(s"User logged in: ${authenticatedUser.username}")
            // Successful login, set the user session and redirect to the home page
            Redirect(routes.HomeController.index()).withSession("user" -> authenticatedUser.username)
          case None =>
            // Log authentication failure
            play.Logger.warn(s"Failed login attempt for user: ${user.username}")
            // Invalid credentials, redirect back to the login page with an error message in flash
            Redirect(routes.LoginController.login()).flashing("error" -> "Invalid username or password")
        }
      } catch {
        case e: Exception =>
          // Handle other exceptions, log them, and redirect to an error page or display a generic error message
          Redirect(routes.LoginController.login()).flashing("error" -> "An error occurred during authentication")
      }
    }.getOrElse {
      // CSRF token is missing, handle accordingly
      BadRequest("CSRF token is missing")
    }
  }

  // Logout: Clear the user session
  def logout: Action[AnyContent] = Action {
    Redirect(routes.LoginController.login()).withNewSession
  }
}