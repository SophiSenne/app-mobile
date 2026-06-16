using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class LikesService(ILikesRepository repository, IPostsRepository postsRepository) : ILikesService
{
    public Task<IReadOnlyList<LikeDto>> GetByPostIdAsync(
        Guid postId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetByPostIdAsync(postId, limit, offset, cancellationToken);

    public Task<IReadOnlyList<LikeDto>> GetByUserIdAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetByUserIdAsync(userId, limit, offset, cancellationToken);

    public Task<bool> HasLikedAsync(Guid userId, Guid postId, CancellationToken cancellationToken) =>
        repository.ExistsAsync(userId, postId, cancellationToken);

    public Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken) =>
        repository.CountByPostIdAsync(postId, cancellationToken);

    public async Task<LikeDto> LikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        var postExists = await postsRepository.ExistsAsync(postId, cancellationToken);
        if (!postExists)
            throw new KeyNotFoundException("Post não encontrado.");

        return await repository.LikeAsync(userId, postId, cancellationToken);
    }

    public Task<bool> UnlikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken) =>
        repository.UnlikeAsync(userId, postId, cancellationToken);
}
