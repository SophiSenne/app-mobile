using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface ICommentsService
{
    Task<IReadOnlyList<CommentDto>> GetByPostIdAsync(Guid postId, int limit, int offset, CancellationToken cancellationToken);

    Task<CommentDto?> GetByIdAsync(Guid commentId, CancellationToken cancellationToken);

    Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken);

    Task<CommentDto> CreateAsync(Guid postId, Guid userId, CreateCommentRequest request, CancellationToken cancellationToken);

    Task<CommentDto?> UpdateAsync(Guid commentId, Guid userId, UpdateCommentRequest request, CancellationToken cancellationToken);

    Task<bool> DeleteAsync(Guid commentId, Guid userId, CancellationToken cancellationToken);
}
