using Hanger.Application.Abstractions;

namespace Hanger.Application.Services;

public sealed class ImageService(IStorageRepository storage) : IImageService
{
    private const string AvatarSubfolder = "avatars";

    /// <inheritdoc />
    public Task<string> UploadAvatarAsync(
        Stream fileStream,
        string fileName,
        string contentType,
        CancellationToken cancellationToken = default)
        => storage.UploadImageAsync(fileStream, fileName, contentType, AvatarSubfolder, cancellationToken);
}
