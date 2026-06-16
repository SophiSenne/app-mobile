using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface IFollowsService
{
    Task<IReadOnlyList<FollowDto>> GetFollowingAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    Task<IReadOnlyList<FollowDto>> GetFollowersAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    Task<bool> IsFollowingAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken);

    Task<(int Followers, int Following)> GetCountsAsync(Guid userId, CancellationToken cancellationToken);

    Task<FollowDto> FollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken);

    Task<bool> UnfollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken);
}
