using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface IHangerRepository
{
    Task<UserDto?> GetUserByIdAsync(Guid userId, CancellationToken cancellationToken);

    Task<(UserDto User, string PasswordHash)?> GetUserByEmailOrUsernameAsync(
        string emailOrUsername,
        CancellationToken cancellationToken);

    Task<UserDto> CreateUserAsync(
        RegisterRequest request,
        string passwordHash,
        CancellationToken cancellationToken);

    Task<bool> DeleteUserAsync(Guid userId, CancellationToken cancellationToken);

    Task<IReadOnlyList<PostDto>> GetAllPostsAsync(CancellationToken cancellationToken);

    Task<IReadOnlyList<PostDto>> GetPostsByUserAsync(Guid userId, CancellationToken cancellationToken);

    Task<IReadOnlyList<PostDto>> SearchPostsByTopicAsync(string topic, CancellationToken cancellationToken);

    Task<PostDto> CreatePostAsync(Guid userId, CreatePostRequest request, CancellationToken cancellationToken);

    Task<PostDto?> UpdatePostAsync(Guid postId, Guid userId, UpdatePostRequest request, CancellationToken cancellationToken);

    Task<bool> DeletePostAsync(Guid postId, Guid userId, CancellationToken cancellationToken);
}
