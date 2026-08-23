import './StarRating.css';

export default function StarRating({ rating, reviewCount, size = 'sm' }) {
  if (rating == null) return null;

  const fullStars = Math.round(rating);

  return (
    <div className={`star-rating star-rating--${size}`}>
      <div className="star-rating__stars" aria-label={`${rating} out of 5 stars`}>
        {[1, 2, 3, 4, 5].map((i) => (
          <svg key={i} width="14" height="14" viewBox="0 0 20 20"
               fill={i <= fullStars ? 'var(--star)' : 'none'}
               stroke="var(--star)" strokeWidth="1">
            <polygon points="10,1 12.6,7 19,7.5 14,12 15.5,18.5 10,15 4.5,18.5 6,12 1,7.5 7.4,7" />
          </svg>
        ))}
      </div>
      {reviewCount != null && (
        <span className="star-rating__count">{reviewCount.toLocaleString()}</span>
      )}
    </div>
  );
}