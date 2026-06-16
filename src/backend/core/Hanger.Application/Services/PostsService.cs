using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class PostsService(IPostsRepository repository) : IPostsService
{
    public Task<IReadOnlyList<PostDto>> GetAllAsync(int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetAllAsync(limit, offset, cancellationToken);

    public Task<IReadOnlyList<PostDto>> GetByUserIdAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetByUserIdAsync(userId, limit, offset, cancellationToken);

    public Task<PostDto?> GetByIdAsync(Guid postId, CancellationToken cancellationToken) =>
        repository.GetByIdAsync(postId, cancellationToken);

    public Task<PostDto> CreateAsync(Guid userId, CreatePostRequest request, CancellationToken cancellationToken) =>
        repository.CreateAsync(userId, request, cancellationToken);

    public Task<PostDto?> UpdateAsync(Guid postId, Guid userId, UpdatePostRequest request, CancellationToken cancellationToken) =>
        repository.UpdateAsync(postId, userId, request, cancellationToken);

    public Task<bool> DeleteAsync(Guid postId, Guid userId, CancellationToken cancellationToken) =>
        repository.DeleteAsync(postId, userId, cancellationToken);
}