import { useParams, useLocation } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../common/Common.css';
import { useNavigate } from 'react-router-dom';

function errorDetail() {
    const { state } = useLocation();
    const errorData = state.data;
	
	// navigation hook
	const navigate = useNavigate();

    return (
        <div className="headerSpacing">
			<button className="btn btn-outline-primary" onClick={() => navigate(-1)}>←</button>
			<div className="lineSpacing"></div>
			
            <div className="px-5">
				<div className="row mb-3">
                    <div className="col-md-6">
                        <strong>ErrorKey:</strong> {errorData.errorKey}
                    </div>
                </div>
				
                <div className="row mb-3">
                    <div className="col-md-6">
                        <strong>Service:</strong> {errorData.service}
                    </div>
                </div>
				
				<div className="row mb-3">
                    <div className="col-md-6">
                        <strong>Function Name:</strong> {errorData.functionName}
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <strong>Error Message:</strong> {errorData.errorMessage}
                    </div>
                </div>
				
				<div className="lineSpacing"></div>
				
				 <div className="row">
                    <div className="col-md-6">
                        <strong>Input:</strong>
                    </div>
                </div>
				
				<div className="row">
                    <div className="col-md-12 card">
                        {errorData.input}
                    </div>
                </div>
				
				<div className="lineSpacing"></div>
				
				<div className="row">
                    <div className="col-md-10">
                        <strong>Trace:</strong>
                    </div>
                </div>
				
				<div className="row">
                    <div className="col-md-12 card">
                        {errorData.stackTrace}
                    </div>
                </div>
            </div>

            <div className="paraSpacing"></div>

        </div>
    )
}

export default errorDetail