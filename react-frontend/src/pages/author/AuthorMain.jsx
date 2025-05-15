import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

const AuthorMain = ({ authors, title}) => {
    const hasAuthors = authors && authors.length > 0;

    return (
        <div className="instructors___page pt---120 pb---140">
            <div className="container pb---60">
            <h6 style={{ marginBottom: "40px" }}>
        profile / {title} / <span style={{ fontWeight: "bold" }}>Achivements</span>
      </h6>
                <div className="row">
                    {hasAuthors ? (
                        authors.slice(0, 8).map((data) => (
                            <div key={data.id} className="col-lg-3">
                                <div className="instructor__content">
                                    <div className="instructor__image">
                                        <img src={data.profileImageUrl.replace("s96-c", "s384-c")} alt={data.name} />
                                    </div>
                                    <div className="bottom-content">
                                        <h4><Link to={`/author/${data.id}`}>{data.name}</Link></h4>
                                    </div>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className="col-12 text-center">
                            <p>No {title}. Follow other users.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

AuthorMain.propTypes = {
    authors: PropTypes.arrayOf(
        PropTypes.shape({
            id: PropTypes.string,
            name: PropTypes.string,
            profileImageUrl: PropTypes.string,
            bio: PropTypes.string,
            currentUserFollows: PropTypes.bool,
        })
    )
};

export default AuthorMain;
