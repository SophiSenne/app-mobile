using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class CommentsService(ICommentsRepository repository, IPostsRepository postsRepository) : ICommentsService
{
    public Task<IReadOnlyList<CommentDto>> GetByPostIdAsync(
        Guid postId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetByPostIdAsync(postId, limit, offset, cancellationToken);

    public Task<CommentDto?> GetByIdAsync(Guid commentId, CancellationToken cancellationToken) =>
        repository.GetByIdAsync(commentId, cancellationToken);

    public Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken) =>
        repository.CountByPostIdAsync(postId, cancellationToken);

    public async Task<CommentDto> CreateAsync(
        Guid postId, Guid userId, CreateCommentRequest request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.Content))
            throw new InvalidOperationException("O conteúdo do comentário não pode ser vazio.");

        var postExists = await postsRepository.ExistsAsync(postId, cancellationToken);
        if (!postExists)
            throw new KeyNotFoundException("Post não encontrado.");

        return await repository.CreateAsync(postId, userId, request, cancellationToken);
    }

    public async Task<CommentDto?> UpdateAsync(
        Guid commentId, Guid userId, UpdateCommentRequest request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.Content))
            throw new InvalidOperationException("O conteúdo do comentário não pode ser vazio.");

        return await repository.UpdateAsync(commentId, userId, request, cancellationToken);
    }

    public Task<bool> DeleteAsync(Guid commentId, Guid userId, CancellationToken cancellationToken) =>
        repository.DeleteAsync(commentId, userId, cancellationToken);
}
