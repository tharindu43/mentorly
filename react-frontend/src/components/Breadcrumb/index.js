// import React from 'react';
// import { Link } from 'react-router-dom';

// import breadcrumbsImg from '../../assets/images/breadcrumbs/1.jpg'

// const Breadcrumb = (props) => {
// 	const { pageTitle } = props;

// 	return (
// 		<div className="react-breadcrumbs">
// 			<div className="breadcrumbs-wrap">
// 				<img className="desktop" src={breadcrumbsImg} alt="Breadcrumbs" />
// 				<img className="mobile" src={breadcrumbsImg} alt="Breadcrumbs" />
// 				<div className="breadcrumbs-inner">
// 					<div className="container">
// 						<div className="breadcrumbs-text">
// 							{/* <h1 className="breadcrumbs-title">{pageTitle ? pageTitle : 'BreadCrumbs'}</h1> */}
// 							<div className="back-nav">
// 								<ul>
// 									<li><Link to="/">Home</Link></li>
// 									<li>{pageTitle ? pageTitle : 'BreadCrumbs'}</li>
// 								</ul>
// 							</div>
// 						</div>
// 					</div>
// 				</div>
// 			</div>                
// 		</div>
// 	);
// }

// export default Breadcrumb;


// import React from 'react';
// import { Link } from 'react-router-dom';

// const Breadcrumb = (props) => {
//   const { pageTitle, breadcrumbItems } = props;
  
//   // Default breadcrumb path if none provided
//   const items = breadcrumbItems || [
//     { label: 'Home', path: '/' },
//     { label: pageTitle || 'Page' }
//   ];

//   return (
//     <div className="breadcrumb-container">
//       <div className="container">
//         <div className="breadcrumb-wrapper">
//           <h1 className="breadcrumb-title">{pageTitle || 'Page Title'}</h1>
//           <nav aria-label="breadcrumb">
//             <ol className="breadcrumb-list">
//               {items.map((item, index) => (
//                 <li key={index} className="breadcrumb-item">
//                   {item.path ? (
//                     <Link to={item.path}>{item.label}</Link>
//                   ) : (
//                     <span className="breadcrumb-active">{item.label}</span>
//                   )}
//                   {index < items.length - 1 && <span className="breadcrumb-separator">/</span>}
//                 </li>
//               ))}
//             </ol>
//           </nav>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default Breadcrumb;

