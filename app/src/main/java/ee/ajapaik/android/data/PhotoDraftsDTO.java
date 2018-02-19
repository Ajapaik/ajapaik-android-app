package ee.ajapaik.android.data;

public class PhotoDraftsDTO {

    private Photo photo;
    private int draftCount;

    public PhotoDraftsDTO(Photo photo, int draftCount) {
        this.photo = photo;
        this.draftCount = draftCount;
    }

    public Photo getPhoto() {
        return photo;
    }

    public int getDraftCount() {
        return draftCount;
    }
}
