using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface IUsersService
{
    Task<UserDto?> GetUserByIdAsync(Guid userId, CancellationToken cancellationToken);

    Task<IReadOnlyList<UserDto>> GetAllAsync(int limit, int offset, CancellationToken cancellationToken);

    Task<UserDto?> UpdateUserAsync(Guid userId, UpdateUserRequest request, CancellationToken cancellationToken);

    Task<UserDto?> PatchUserAsync(Guid userId, PatchUserRequest request, CancellationToken cancellationToken);

    Task<bool> DeleteAccountAsync(Guid userId, CancellationToken cancellationToken);
}