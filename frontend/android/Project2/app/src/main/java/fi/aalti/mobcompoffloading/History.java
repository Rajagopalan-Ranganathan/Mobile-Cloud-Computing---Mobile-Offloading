package fi.aalti.mobcompoffloading;


import java.util.ArrayList;

public class History {
    public History(ArrayList<String> thumbnails, ArrayList<String> sources, ArrayList<String> ocr, ArrayList<String> benchmarks, String creationTime) {
        this.thumbnails = thumbnails;
        this.sources = sources;
        this.ocr = ocr;
        this.benchmarks = benchmarks;
        this.creationTime = creationTime;
    }

    private ArrayList<String> thumbnails;
    private ArrayList<String> sources;
    private ArrayList<String> ocr;
    private ArrayList<String> benchmarks;
    private String creationTime;


    public ArrayList<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(ArrayList<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public ArrayList<String> getSources() {
        return sources;
    }

    public void setSources(ArrayList<String> sources) {
        this.sources = sources;
    }

    public ArrayList<String> getOcr() {
        return ocr;
    }

    public void setOcr(ArrayList<String> ocr) {
        this.ocr = ocr;
    }

    public ArrayList<String> getBenchmarks() {
        return benchmarks;
    }

    public void setBenchmarks(ArrayList<String> benchmarks) {
        this.benchmarks = benchmarks;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
    public String getTruncatedOCR()
    {
        String trunc = "";
        try {
            for (String item : ocr) {
                trunc += item;
            }
            trunc = trunc.substring(0, 100);

        }catch(Exception ex){}

        return trunc;
    }
}


