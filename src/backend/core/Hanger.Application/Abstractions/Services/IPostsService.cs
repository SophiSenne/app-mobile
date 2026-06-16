using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface IPostsService
{
    Task<IReadOnlyList<PostDto>> GetAllAsync(int limit, int offset, CancellationToken cancellationToken);

    Task<IReadOnlyList<PostDto>> GetByUserIdAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    Task<PostDto?> GetByIdAsync(Guid postId, CancellationToken cancellationToken);

    Task<PostDto> CreateAsync(Guid userId, CreatePostRequest request, CancellationToken cancellationToken);

    Task<PostDto?> UpdateAsync(Guid postId, Guid userId, UpdatePostRequest request, CancellationToken cancellationToken);

    Task<bool> DeleteAsync(Guid postId, Guid userId, CancellationToken cancellationToken);
}