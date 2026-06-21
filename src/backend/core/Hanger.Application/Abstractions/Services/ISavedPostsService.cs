using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface ISavedPostsService
{
    Task<IReadOnlyList<PostDto>> GetByUserIdAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    Task<bool> HasSavedAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    Task<int> CountByUserIdAsync(Guid userId, CancellationToken cancellationToken);

    Task<SavedPostDto> SaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    Task<bool> UnsaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken);
}
