using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface IUsersRepository
{
    Task<UserDto?> GetUserByIdAsync(Guid userId, CancellationToken cancellationToken);

    Task<UserDto?> GetUserByEmailAsync(string email, CancellationToken cancellationToken);

    Task<UserDto?> GetUserByUsernameAsync(string username, CancellationToken cancellationToken);

    Task<IReadOnlyList<UserDto>> GetAllAsync(int limit, int offset, CancellationToken cancellationToken);

    Task<UserDto> CreateAsync(CreateUserRequest request, CancellationToken cancellationToken);

    Task<UserDto?> UpdateAsync(Guid id, UpdateUserRequest request, CancellationToken cancellationToken);

    Task<UserDto?> UpdatePartialAsync(Guid id, PatchUserRequest request, CancellationToken cancellationToken);

    Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken);

    Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken);

    Task<bool> EmailExistsAsync(string email, CancellationToken cancellationToken);

    Task<bool> UsernameExistsAsync(string username, CancellationToken cancellationToken);
}