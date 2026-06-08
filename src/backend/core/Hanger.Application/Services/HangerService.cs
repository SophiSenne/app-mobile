using System.Security.Cryptography;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public interface IHangerService
{
    Task<AuthResponse> RegisterAsync(RegisterRequest request, CancellationToken cancellationToken);

    Task<AuthResponse?> LoginAsync(LoginRequest request, CancellationToken cancellationToken);

    Task<UserDto?> GetMeAsync(Guid userId, CancellationToken cancellationToken);

    Task<bool> DeleteAccountAsync(Guid userId, CancellationToken cancellationToken);

    Task<IReadOnlyList<PostDto>> GetAllPostsAsync(CancellationToken cancellationToken);

    Task<IReadOnlyList<PostDto>> GetPostsByUserAsync(Guid userId, CancellationToken cancellationToken);

    Task<IReadOnlyList<PostDto>> SearchPostsByTopicAsync(string topic, CancellationToken cancellationToken);

    Task<PostDto> CreatePostAsync(Guid userId, CreatePostRequest request, CancellationToken cancellationToken);

    Task<PostDto?> UpdatePostAsync(Guid postId, Guid userId, UpdatePostRequest request, CancellationToken cancellationToken);

    Task<bool> DeletePostAsync(Guid postId, Guid userId, CancellationToken cancellationToken);
}

public sealed class HangerService(IHangerRepository repository) : IHangerService
{
    public async Task<AuthResponse> RegisterAsync(RegisterRequest request, CancellationToken cancellationToken)
    {
        ValidateRequired(request.Username, "username");
        ValidateRequired(request.Email, "email");
        ValidateRequired(request.Password, "password");

        if (request.Password.Length < 6)
        {
            throw new ArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }

        var user = await repository.CreateUserAsync(request, HashPassword(request.Password), cancellationToken);
        return new AuthResponse(user);
    }

    public async Task<AuthResponse?> LoginAsync(LoginRequest request, CancellationToken cancellationToken)
    {
        ValidateRequired(request.EmailOrUsername, "emailOrUsername");
        ValidateRequired(request.Password, "password");

        var found = await repository.GetUserByEmailOrUsernameAsync(request.EmailOrUsername, cancellationToken);
        if (found is null || !VerifyPassword(request.Password, found.Value.PasswordHash))
        {
            return null;
        }

        return new AuthResponse(found.Value.User);
    }

    public Task<UserDto?> GetMeAsync(Guid userId, CancellationToken cancellationToken) =>
        repository.GetUserByIdAsync(userId, cancellationToken);

    public Task<bool> DeleteAccountAsync(Guid userId, CancellationToken cancellationToken) =>
        repository.DeleteUserAsync(userId, cancellationToken);

    public Task<IReadOnlyList<PostDto>> GetAllPostsAsync(CancellationToken cancellationToken) =>
        repository.GetAllPostsAsync(cancellationToken);

    public Task<IReadOnlyList<PostDto>> GetPostsByUserAsync(Guid userId, CancellationToken cancellationToken) =>
        repository.GetPostsByUserAsync(userId, cancellationToken);

    public Task<IReadOnlyList<PostDto>> SearchPostsByTopicAsync(string topic, CancellationToken cancellationToken)
    {
        ValidateRequired(topic, "topic");
        return repository.SearchPostsByTopicAsync(topic.Trim(), cancellationToken);
    }

    public Task<PostDto> CreatePostAsync(Guid userId, CreatePostRequest request, CancellationToken cancellationToken)
    {
        ValidateRequired(request.ImageUrl, "imageUrl");
        ValidateRequired(request.Title, "title");
        return repository.CreatePostAsync(userId, request, cancellationToken);
    }

    public Task<PostDto?> UpdatePostAsync(
        Guid postId,
        Guid userId,
        UpdatePostRequest request,
        CancellationToken cancellationToken) =>
        repository.UpdatePostAsync(postId, userId, request, cancellationToken);

    public Task<bool> DeletePostAsync(Guid postId, Guid userId, CancellationToken cancellationToken) =>
        repository.DeletePostAsync(postId, userId, cancellationToken);

    private static void ValidateRequired(string? value, string fieldName)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            throw new ArgumentException($"Informe o campo '{fieldName}'.");
        }
    }

    private static string HashPassword(string password)
    {
        var salt = RandomNumberGenerator.GetBytes(16);
        var hash = Rfc2898DeriveBytes.Pbkdf2(password, salt, 100_000, HashAlgorithmName.SHA256, 32);
        return $"pbkdf2-sha256$100000${Convert.ToBase64String(salt)}${Convert.ToBase64String(hash)}";
    }

    private static bool VerifyPassword(string password, string storedHash)
    {
        var parts = storedHash.Split('$');
        if (parts.Length != 4 || parts[0] != "pbkdf2-sha256" || !int.TryParse(parts[1], out var iterations))
        {
            return false;
        }

        var salt = Convert.FromBase64String(parts[2]);
        var expectedHash = Convert.FromBase64String(parts[3]);
        var actualHash = Rfc2898DeriveBytes.Pbkdf2(password, salt, iterations, HashAlgorithmName.SHA256, expectedHash.Length);

        return CryptographicOperations.FixedTimeEquals(actualHash, expectedHash);
    }
}
