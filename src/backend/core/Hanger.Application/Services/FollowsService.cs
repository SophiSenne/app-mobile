using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class FollowsService(
    IFollowsRepository repository,
    IUsersRepository usersRepository,
    INotificationsService notificationsService) : IFollowsService
{
    public Task<IReadOnlyList<FollowDto>> GetFollowingAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetFollowingAsync(userId, limit, offset, cancellationToken);

    public Task<IReadOnlyList<FollowDto>> GetFollowersAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetFollowersAsync(userId, limit, offset, cancellationToken);

    public Task<bool> IsFollowingAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken) =>
        repository.ExistsAsync(followerId, followingId, cancellationToken);

    public Task<(int Followers, int Following)> GetCountsAsync(Guid userId, CancellationToken cancellationToken) =>
        repository.GetCountsAsync(userId, cancellationToken);

    public async Task<FollowDto> FollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken)
    {
        if (followerId == followingId)
            throw new InvalidOperationException("Um usuário não pode seguir a si mesmo.");

        var targetExists = await usersRepository.ExistsAsync(followingId, cancellationToken);
        if (!targetExists)
            throw new KeyNotFoundException("Usuário a ser seguido não encontrado.");

        var follow = await repository.FollowAsync(followerId, followingId, cancellationToken);

        await notificationsService.CreateAsync(
            recipientId: followingId,
            senderId: followerId,
            type: "follow",
            postId: null,
            cancellationToken);

        return follow;
    }

    public Task<bool> UnfollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken) =>
        repository.UnfollowAsync(followerId, followingId, cancellationToken);
}
