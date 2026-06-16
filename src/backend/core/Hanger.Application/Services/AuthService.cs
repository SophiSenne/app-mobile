using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class AuthService(IAuthRepository repository) : IAuthService
{
    public async Task<AuthResponse> RegisterAsync(RegisterRequest request, CancellationToken cancellationToken)
    {
        if (await repository.EmailExistsAsync(request.Email, cancellationToken))
            throw new InvalidOperationException("E-mail já cadastrado.");

        if (await repository.UsernameExistsAsync(request.Username, cancellationToken))
            throw new InvalidOperationException("Username já cadastrado.");

        var passwordHash = BCrypt.Net.BCrypt.HashPassword(request.Password);

        var user = await repository.CreateUserAsync(request, passwordHash, cancellationToken);

        return new AuthResponse(user);
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request, CancellationToken cancellationToken)
    {
        var credentials = await repository.GetCredentialsByEmailOrUsernameAsync(
            request.EmailOrUsername, cancellationToken);

        if (credentials is null || !BCrypt.Net.BCrypt.Verify(request.Password, credentials.Value.PasswordHash))
            throw new UnauthorizedAccessException("Credenciais inválidas.");

        return new AuthResponse(credentials.Value.User);
    }
}