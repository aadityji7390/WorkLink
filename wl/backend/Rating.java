import java.util.List;

public class Rating {

    // =========================================
    // ADD NEW RATING
    // =========================================

    public boolean addRating(
            String raterType,
            String raterPhone,
            String raterName,
            String targetType,
            String targetPhone,
            String targetName,
            int rating,
            String review
    ) {

        if (
                rating < 1
                ||
                rating > 5
        ) {

            return false;
        }


        if (
                raterType == null
                ||
                raterPhone == null
                ||
                raterName == null
                ||
                targetType == null
                ||
                targetPhone == null
                ||
                targetName == null
        ) {

            return false;
        }


        return FileManager.saveRating(
                raterType,
                raterPhone,
                raterName,
                targetType,
                targetPhone,
                targetName,
                rating,
                review == null ? "" : review
        );
    }


    // =========================================
    // GET ALL
    // =========================================

    public List<String> getAllRatings() {

        return FileManager.getRatings();
    }


    // =========================================
    // WORKER AVERAGE
    // =========================================

    public double getWorkerAverageRating(
            String workerName
    ) {

        return FileManager.getWorkerAverageRating(
                workerName
        );
    }


    // =========================================
    // TARGET AVERAGE
    // =========================================

    public double getAverageRating(
            String targetType,
            String targetPhone,
            String targetName
    ) {

        return FileManager.getAverageRating(
                targetType,
                targetPhone,
                targetName
        );
    }
}