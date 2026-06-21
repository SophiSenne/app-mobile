using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class SavedPostsService(ISavedPostsRepository repository, IPostsRepository postsRepository) : ISavedPostsService
{
    public Task<IReadOnlyList<PostDto>> GetByUserIdAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetByUserIdAsync(userId, limit, offset, cancellationToken);

    public Task<bool> HasSavedAsync(Guid userId, Guid postId, CancellationToken cancellationToken) =>
        repository.ExistsAsync(userId, postId, cancellationToken);

    public Task<int> CountByUserIdAsync(Guid userId, CancellationToken cancellationToken) =>
        repository.CountByUserIdAsync(userId, cancellationToken);

    public async Task<SavedPostDto> SaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        var postExists = await postsRepository.ExistsAsync(postId, cancellationToken);
        if (!postExists)
            throw new KeyNotFoundException("Post não encontrado.");

        return await repository.SaveAsync(userId, postId, cancellationToken);
    }

    public Task<bool> UnsaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken) =>
        repository.UnsaveAsync(userId, postId, cancellationToken);
}
