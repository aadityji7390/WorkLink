import java.util.List;
public class Rating {
    public boolean addRating(String raterType,String raterPhone,String raterName,String targetType,String targetPhone,String targetName,int rating,String review) {
        if (rating < 1 || rating > 5 || raterType == null || targetType == null) return false;
        return FileManager.saveRating(raterType,raterPhone,raterName,targetType,targetPhone,targetName,rating,review == null ? "" : review);
    }
    public List<String> getAllRatings() { return FileManager.getRatings(); }
    public double getWorkerAverageRating(String workerName) { return FileManager.getWorkerAverageRating(workerName); }
    public double getAverageRating(String targetType,String targetPhone,String targetName) { return FileManager.getAverageRating(targetType,targetPhone,targetName); }
}
