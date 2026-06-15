using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class UsersService(IUsersRepository repository) : IUsersService
{
    public Task<UserDto?> GetUserByIdAsync(Guid userId, CancellationToken cancellationToken) =>
        repository.GetUserByIdAsync(userId, cancellationToken);

    public Task<IReadOnlyList<UserDto>> GetAllAsync(int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetAllAsync(limit, offset, cancellationToken);

    public Task<UserDto?> UpdateUserAsync(Guid userId, UpdateUserRequest request, CancellationToken cancellationToken) =>
        repository.UpdateAsync(userId, request, cancellationToken);

    public Task<UserDto?> PatchUserAsync(Guid userId, PatchUserRequest request, CancellationToken cancellationToken) =>
        repository.UpdatePartialAsync(userId, request, cancellationToken);

    public Task<bool> DeleteAccountAsync(Guid userId, CancellationToken cancellationToken) =>
        repository.DeleteAsync(userId, cancellationToken);
}