namespace Hanger.Application.Abstractions;

public interface IImageService
{
    /// <summary>
    /// Faz upload de uma foto de perfil para a pasta "avatars" no Supabase Storage
    /// e retorna a URL pública para ser usada como avatar_url do usuário.
    /// </summary>
    Task<string> UploadAvatarAsync(
        Stream fileStream,
        string fileName,
        string contentType,
        CancellationToken cancellationToken = default);
}
