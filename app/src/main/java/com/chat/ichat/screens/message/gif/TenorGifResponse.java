package com.chat.ichat.screens.message.gif;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 29/05/17.
 */

public class TenorGifResponse {
    @SerializedName("results")
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    class Result {
        @SerializedName("media")
        private List<Media> media;

        public Media getMedia() {
            return media.get(0);
        }

        public Media.Gif getGif() {
            return getMedia().getGif();
        }

        public Media.Gif getNanoGif() {
            return getMedia().getNanoGif();
        }

        public Media.Gif getMediumGif() {
            return getMedia().getMediumGif();
        }

        private class Media {
            @SerializedName("gif")
            private Gif gif;
            @SerializedName("nanogif")
            private Gif nanoGif;
            @SerializedName("mediumgif")
            private Gif mediumGif;

            public Gif getGif() {
                return gif;
            }

            public Gif getNanoGif() {
                return nanoGif;
            }

            public Gif getMediumGif() {
                return mediumGif;
            }

            class Gif {
                @SerializedName("url")
                private String url;
                @SerializedName("preview")
                private String preview;
                @SerializedName("dims")
                private List<Integer> dims;

                public String getUrl() {
                    return url;
                }

                public String getPreview() {
                    return preview;
                }

                public int getWidth() {
                    return dims.get(0);
                }

                public int getHeight() {
                    return dims.get(1);
                }

                @Override
                public String toString() {
                    return "Gif{" +
                            "url='" + url + '\'' +
                            ", preview='" + preview + '\'' +
                            ", dims=" + dims +
                            '}';
                }
            }
        }
    }
}
