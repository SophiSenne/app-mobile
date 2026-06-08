using Hanger.Application.DTOs;
using Hanger.Application.Services;
using Microsoft.AspNetCore.Mvc;
using Npgsql;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("auth")]
public sealed class AuthController(IHangerService service) : ApiControllerBase
{
    /// <summary>Cadastro de usuario.</summary>
    [HttpPost("register")]
    [ProducesResponseType<AuthResponse>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Register(RegisterRequest request, CancellationToken cancellationToken)
    {
        try
        {
            var response = await service.RegisterAsync(request, cancellationToken);
            return CreatedAtAction("GetMe", "Users", new { userId = response.User.Id }, response);
        }
        catch (PostgresException exception) when (exception.SqlState == PostgresErrorCodes.UniqueViolation)
        {
            return BadRequestProblem("Username ou email ja cadastrado.");
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    /// <summary>Login por email ou username.</summary>
    [HttpPost("login")]
    [ProducesResponseType<AuthResponse>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> Login(LoginRequest request, CancellationToken cancellationToken)
    {
        try
        {
            var response = await service.LoginAsync(request, cancellationToken);
            return response is null
                ? UnauthorizedProblem("Email/username ou senha invalidos.")
                : Ok(response);
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    /// <summary>Logout. Nao ha sessao server-side nesta versao.</summary>
    [HttpPost("logout")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    public IActionResult Logout() => NoContent();
}
