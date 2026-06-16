namespace Hanger.Application.DTOs;


public sealed record LoginRequest(string EmailOrUsername, string Password);

public sealed record AuthResponse(UserDto User);

public sealed record CreatePostRequest(
    string ImageUrl,
    string Title,
    string? Caption,
    string? WeatherCondition,
    double? Temperature,
    string? City,
    int? CategoryId,
    int? TypeId);

public sealed record UpdatePostRequest(
    string? ImageUrl,
    string? Title,
    string? Caption,
    string? WeatherCondition,
    double? Temperature,
    string? City,
    int? CategoryId,
    int? TypeId);

public sealed record PostDto(
    Guid Id,
    Guid UserId,
    string Username,
    string ImageUrl,
    string Title,
    string? Caption,
    string? WeatherCondition,
    double? Temperature,
    string? City,
    int ShareCount,
    DateTime CreatedAt,
    int? CategoryId,
    string? CategoryName,
    int? TypeId,
    string? TypeName);


// ============================================================
//  FOLLOWS
// ============================================================

public sealed record FollowRequest(Guid FollowingId);

public sealed record FollowDto(
    Guid FollowerId,
    string FollowerUsername,
    Guid FollowingId,
    string FollowingUsername,
    DateTime CreatedAt);

// ============================================================
//  LIKES
// ============================================================

public sealed record LikeDto(
    Guid UserId,
    string Username,
    Guid PostId,
    DateTime CreatedAt);

// ============================================================
//  COMMENTS
// ============================================================

public sealed record CreateCommentRequest(string Content);

public sealed record UpdateCommentRequest(string Content);

public sealed record CommentDto(
    Guid Id,
    Guid PostId,
    Guid UserId,
    string Username,
    string? AvatarUrl,
    string Content,
    DateTime CreatedAt);

// ============================================================
//  NOTIFICATIONS
// ============================================================

public sealed record NotificationDto(
    Guid Id,
    Guid RecipientId,
    Guid SenderId,
    string SenderUsername,
    string? SenderAvatarUrl,
    string Type,
    Guid? PostId,
    bool Read,
    DateTime CreatedAt);

// ============================================================
//  CATEGORIES & TYPES
// ============================================================

public sealed record CategoryDto(int Id, string Name, IReadOnlyList<TypeDto>? Types = null);

public sealed record TypeDto(int Id, int CategoryId, string CategoryName, string Name);

public sealed record CreateCategoryRequest(string Name);

public sealed record CreateTypeRequest(int CategoryId, string Name);

public sealed record UpdateCategoryRequest(string Name);

public sealed record UpdateTypeRequest(string Name);
