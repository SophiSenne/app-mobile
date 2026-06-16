using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface ILikesService
{
    Task<IReadOnlyList<LikeDto>> GetByPostIdAsync(Guid postId, int limit, int offset, CancellationToken cancellationToken);

    Task<IReadOnlyList<LikeDto>> GetByUserIdAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    Task<bool> HasLikedAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken);

    Task<LikeDto> LikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    Task<bool> UnlikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken);
}
