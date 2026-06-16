using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface IAuthRepository
{
    /// <summary>Busca usuário e seu hash de senha por e-mail ou username.</summary>
    Task<(UserDto User, string PasswordHash)?> GetCredentialsByEmailOrUsernameAsync(
        string emailOrUsername,
        CancellationToken cancellationToken);

    /// <summary>Cria um novo usuário e retorna seus dados públicos.</summary>
    Task<UserDto> CreateUserAsync(
        RegisterRequest request,
        string passwordHash,
        CancellationToken cancellationToken);

    /// <summary>Verifica se um e-mail já está cadastrado.</summary>
    Task<bool> EmailExistsAsync(string email, CancellationToken cancellationToken);

    /// <summary>Verifica se um username já está cadastrado.</summary>
    Task<bool> UsernameExistsAsync(string username, CancellationToken cancellationToken);
}