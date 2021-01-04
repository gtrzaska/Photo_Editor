package com.example.photoeditor;

public class Upload {
    private String mUser;
    private String mImageUrl;
    private boolean mIsPublic;
    private int mLikesNumber;

    public Upload() {

    }

    public Upload(String user, String imageUrl, boolean isPublic, int likesNumber) {
        mUser = user;
        mImageUrl = imageUrl;
        mIsPublic = isPublic;
        mLikesNumber = likesNumber;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public boolean getIsPublic() {
        return mIsPublic;
    }

    public void setIsPublic(boolean isPublic) {
        mIsPublic = isPublic;
    }

    public int getLikesNumber() {
        return mLikesNumber;
    }

    public void setLikesNumber(int likesNumber) {
        mLikesNumber = likesNumber;
    }
}

