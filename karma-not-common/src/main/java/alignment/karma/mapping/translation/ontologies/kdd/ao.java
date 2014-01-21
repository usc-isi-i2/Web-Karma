/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class ao
{
	private static final String PREFIX = "ao";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource AIRSQueryToReQueryWorkflow = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AIRSQueryToReQueryWorkflow");
	public static final Resource AbductiveRequeryInputData = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequeryInputData");
	public static final Resource AbductiveRequeryInputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequeryInputDataModel");
	public static final Resource AbductiveRequeryOutputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequeryOutputDataModel");
	public static final Resource AbductiveRequeryOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequeryOutputDataSet");
	public static final Resource AbductiveRequerySubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequerySubroutine");
	public static final Resource AbductiveRequerySubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequerySubroutineExecution");
	public static final Resource AbductiveRequerySubroutineMeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequerySubroutineMeanHardDriveUsageMeasurement");
	public static final Resource AbductiveRequerySubroutineMeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#AbductiveRequerySubroutineMeanHardDriveUsageMeasurementBearer");
	public static final Resource ActOfComputingSystemUsage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ActOfComputingSystemUsage");
	public static final Resource ComputerHardDrive = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ComputerHardDrive");
	public static final Resource ComputingFolder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ComputingFolder");
	public static final Resource ComputingSystem = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ComputingSystem");
	public static final Resource ComputingSystemAvailabilityRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ComputingSystemAvailabilityRatioMeasurement");
	public static final Resource ComputingSystemAvailabilityRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ComputingSystemAvailabilityRatioMeasurementBearer");
	public static final Resource ComputingSystemRuntime = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ComputingSystemRuntime");
	public static final Resource ComputingSystemUptime = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ComputingSystemUptime");
	public static final Resource DataAssociationInputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationInputDataModel");
	public static final Resource DataAssociationInputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationInputDataSet");
	public static final Resource DataAssociationOutputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationOutputDataModel");
	public static final Resource DataAssociationOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationOutputDataSet");
	public static final Resource DataAssociationSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationSubroutine");
	public static final Resource DataAssociationSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationSubroutineExecution");
	public static final Resource DataAssociationSubroutineMeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationSubroutineMeanHardDriveUsageMeasurement");
	public static final Resource DataAssociationSubroutineMeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataAssociationSubroutineMeanHardDriveUsageMeasurementBearer");
	public static final Resource DataElementFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementFunction");
	public static final Resource DataElementMergeFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementMergeFunction");
	public static final Resource DataElementMergeInputDataElement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementMergeInputDataElement");
	public static final Resource DataElementMergeInputData_Model = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementMergeInputData_Model");
	public static final Resource DataElementMergeOutputDataElement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementMergeOutputDataElement");
	public static final Resource DataElementMergeOutputData_Model = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementMergeOutputData_Model");
	public static final Resource DataElementMergeSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementMergeSubroutine");
	public static final Resource DataElementMergeSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataElementMergeSubroutineExecution");
	public static final Resource DataSetMergeInputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeInputDataModel");
	public static final Resource DataSetMergeInputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeInputDataSet");
	public static final Resource DataSetMergeOutputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeOutputDataModel");
	public static final Resource DataSetMergeOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeOutputDataSet");
	public static final Resource DataSetMergeSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeSubroutine");
	public static final Resource DataSetMergeSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeSubroutineExecution");
	public static final Resource DataSetMergeSubroutineMeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeSubroutineMeanHardDriveUsageMeasurement");
	public static final Resource DataSetMergeSubroutineMeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DataSetMergeSubroutineMeanHardDriveUsageMeasurementBearer");
	public static final Resource DistinctSetFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#DistinctSetFunction");
	public static final Resource ElementAtFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ElementAtFunction");
	public static final Resource ElementAtOrDefaultFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ElementAtOrDefaultFunction");
	public static final Resource ExceptSetFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ExceptSetFunction");
	public static final Resource ExceptionConditionSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ExceptionConditionSpecification");
	public static final Resource FileSize = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#FileSize");
	public static final Resource FileSizeRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#FileSizeRatioMeasurement");
	public static final Resource FileSizeRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#FileSizeRatioMeasurementBearer");
	public static final Resource HardDriveUsageRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#HardDriveUsageRatioMeasurement");
	public static final Resource HardDriveUsageRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#HardDriveUsageRatioMeasurementBearer");
	public static final Resource IAggregation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IAggregation");
	public static final Resource IContract = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IContract");
	public static final Resource ICount = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ICount");
	public static final Resource IDynamicModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IDynamicModel");
	public static final Resource IGlobalModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IGlobalModel");
	public static final Resource IInvariant = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IInvariant");
	public static final Resource IInvariantVariable = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IInvariantVariable");
	public static final Resource ILinq = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ILinq");
	public static final Resource IModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IModel");
	public static final Resource IState = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IState");
	public static final Resource InformationBearingEntityAsInputRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#InformationBearingEntityAsInputRole");
	public static final Resource InformationBearingEntityAsOutputRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#InformationBearingEntityAsOutputRole");
	public static final Resource InformationBearingEntityRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#InformationBearingEntityRole");
	public static final Resource InputDataElement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#InputDataElement");
	public static final Resource InputSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#InputSpecification");
	public static final Resource IntersectionSetFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#IntersectionSetFunction");
	public static final Resource MapSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MapSubroutine");
	public static final Resource MapperCount = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MapperCount");
	public static final Resource MapperCountRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MapperCountRatioMeasurement");
	public static final Resource MapperCountRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MapperCountRatioMeasurementBearer");
	public static final Resource MaximumSoftwareSubroutineExecutionMemoryUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MaximumSoftwareSubroutineExecutionMemoryUsageMeasurement");
	public static final Resource MaximumSoftwareSubroutineExecutionMemoryUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MaximumSoftwareSubroutineExecutionMemoryUsageMeasurementBearer");
	public static final Resource MeanComputingSystemAvailabilityMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanComputingSystemAvailabilityMeasurement");
	public static final Resource MeanComputingSystemAvailabilityMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanComputingSystemAvailabilityMeasurementBearer");
	public static final Resource MeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanHardDriveUsageMeasurement");
	public static final Resource MeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanHardDriveUsageMeasurementBearer");
	public static final Resource MeanProcessingLoadMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanProcessingLoadMeasurement");
	public static final Resource MeanProcessingLoadMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanProcessingLoadMeasurementBearer");
	public static final Resource MeanSoftwareSubroutineExecutionMemoryUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanSoftwareSubroutineExecutionMemoryUsageMeasurement");
	public static final Resource MeanSoftwareSubroutineExecutionMemoryUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanSoftwareSubroutineExecutionMemoryUsageMeasurementBearer");
	public static final Resource MeanSoftwareSubroutineExecutionTimeMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanSoftwareSubroutineExecutionTimeMeasurement");
	public static final Resource MeanSoftwareSubroutineExecutionTimeMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#MeanSoftwareSubroutineExecutionTimeMeasurementBearer");
	public static final Resource ObjectInvariantSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ObjectInvariantSpecification");
	public static final Resource OutputDataElement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#OutputDataElement");
	public static final Resource OutputSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#OutputSpecification");
	public static final Resource PerformanceSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#PerformanceSpecification");
	public static final Resource PostconditionSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#PostconditionSpecification");
	public static final Resource PreconditionSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#PreconditionSpecification");
	public static final Resource ProcessingElementFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ProcessingElementFunction");
	public static final Resource ProcessingLoadRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ProcessingLoadRatioMeasurement");
	public static final Resource ProcessingLoadRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ProcessingLoadRatioMeasurementBearer");
	public static final Resource ProcessingSlot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ProcessingSlot");
	public static final Resource ProcessingSlotCount = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ProcessingSlotCount");
	public static final Resource ProcessingSlotCountRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ProcessingSlotCountRatioMeasurement");
	public static final Resource ProcessingSlotCountRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ProcessingSlotCountRatioMeasurementBearer");
	public static final Resource QueryInputData = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QueryInputData");
	public static final Resource QueryInputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QueryInputDataModel");
	public static final Resource QueryOutputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QueryOutputDataModel");
	public static final Resource QueryOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QueryOutputDataSet");
	public static final Resource QuerySubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QuerySubroutine");
	public static final Resource QuerySubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QuerySubroutineExecution");
	public static final Resource QuerySubroutineMeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QuerySubroutineMeanHardDriveUsageMeasurement");
	public static final Resource QuerySubroutineMeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#QuerySubroutineMeanHardDriveUsageMeasurementBearer");
	public static final Resource SemiconductorMemory = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SemiconductorMemory");
	public static final Resource ServiceInterface = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#ServiceInterface");
	public static final Resource SetElementFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SetElementFunction");
	public static final Resource SetFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SetFunction");
	public static final Resource SideEffectSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SideEffectSpecification");
	public static final Resource SoftwareSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutine");
	public static final Resource SoftwareSubroutineDecisionNodeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineDecisionNodeNominalMeasurement");
	public static final Resource SoftwareSubroutineDecisionNodeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineDecisionNodeNominalMeasurementBearer");
	public static final Resource SoftwareSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecution");
	public static final Resource SoftwareSubroutineExecutionMemoryUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecutionMemoryUsageMeasurement");
	public static final Resource SoftwareSubroutineExecutionMemoryUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecutionMemoryUsageMeasurementBearer");
	public static final Resource SoftwareSubroutineExecutionOrder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecutionOrder");
	public static final Resource SoftwareSubroutineExecutionOrderOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecutionOrderOrdinalMeasurement");
	public static final Resource SoftwareSubroutineExecutionOrderOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecutionOrderOrdinalMeasurementBearer");
	public static final Resource SoftwareSubroutineExecutionTimeMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecutionTimeMeasurement");
	public static final Resource SoftwareSubroutineExecutionTimeMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineExecutionTimeMeasurementBearer");
	public static final Resource SoftwareSubroutineIdentifier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineIdentifier");
	public static final Resource SoftwareSubroutineIdentifierBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineIdentifierBearer");
	public static final Resource SoftwareSubroutineUtilityIntervalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineUtilityIntervalMeasurement");
	public static final Resource SoftwareSubroutineUtilityIntervalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#SoftwareSubroutineUtilityIntervalMeasurementBearer");
	public static final Resource StandardDeviationComputingSystemAvailabilityMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationComputingSystemAvailabilityMeasurement");
	public static final Resource StandardDeviationComputingSystemAvailabilityMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationComputingSystemAvailabilityMeasurementBearer");
	public static final Resource StandardDeviationHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationHardDriveUsageMeasurement");
	public static final Resource StandardDeviationHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationHardDriveUsageMeasurementBearer");
	public static final Resource StandardDeviationProcessingLoadMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationProcessingLoadMeasurement");
	public static final Resource StandardDeviationProcessingLoadMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationProcessingLoadMeasurementBearer");
	public static final Resource StandardDeviationSoftwareSubroutineExecutionMemoryUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationSoftwareSubroutineExecutionMemoryUsageMeasurement");
	public static final Resource StandardDeviationSoftwareSubroutineExecutionMemoryUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationSoftwareSubroutineExecutionMemoryUsageMeasurementBearer");
	public static final Resource StandardDeviationSoftwareSubroutineExecutionTimeMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationSoftwareSubroutineExecutionTimeMeasurement");
	public static final Resource StandardDeviationSoftwareSubroutineExecutionTimeMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StandardDeviationSoftwareSubroutineExecutionTimeMeasurementBearer");
	public static final Resource StructuredDataToRDFInputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFInputDataModel");
	public static final Resource StructuredDataToRDFInputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFInputDataSet");
	public static final Resource StructuredDataToRDFOutputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFOutputDataModel");
	public static final Resource StructuredDataToRDFOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFOutputDataSet");
	public static final Resource StructuredDataToRDFSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFSubroutine");
	public static final Resource StructuredDataToRDFSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFSubroutineExecution");
	public static final Resource StructuredDataToRDFSubroutineMeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFSubroutineMeanHardDriveUsageMeasurement");
	public static final Resource StructuredDataToRDFSubroutineMeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#StructuredDataToRDFSubroutineMeanHardDriveUsageMeasurementBearer");
	public static final Resource TrustInputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustInputDataModel");
	public static final Resource TrustInputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustInputDataSet");
	public static final Resource TrustOutputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustOutputDataModel");
	public static final Resource TrustOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustOutputDataSet");
	public static final Resource TrustSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustSubroutine");
	public static final Resource TrustSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustSubroutineExecution");
	public static final Resource TrustSubroutineMeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustSubroutineMeanHardDriveUsageMeasurement");
	public static final Resource TrustSubroutineMeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#TrustSubroutineMeanHardDriveUsageMeasurementBearer");
	public static final Resource UnionSetFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnionSetFunction");
	public static final Resource UnionSetInputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnionSetInputDataSet");
	public static final Resource UnionSetInputDataSet_Model = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnionSetInputDataSet_Model");
	public static final Resource UnionSetOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnionSetOutputDataSet");
	public static final Resource UnionSetOutputDataSet_Model = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnionSetOutputDataSet_Model");
	public static final Resource UnionSetSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnionSetSubroutine");
	public static final Resource UnionSetSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnionSetSubroutineExecution");
	public static final Resource UnstructuredDataToRDFInputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFInputDataModel");
	public static final Resource UnstructuredDataToRDFInputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFInputDataSet");
	public static final Resource UnstructuredDataToRDFOutputDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFOutputDataModel");
	public static final Resource UnstructuredDataToRDFOutputDataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFOutputDataSet");
	public static final Resource UnstructuredDataToRDFSubroutine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFSubroutine");
	public static final Resource UnstructuredDataToRDFSubroutineExecution = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFSubroutineExecution");
	public static final Resource UnstructuredDataToRDFSubroutineMeanHardDriveUsageMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFSubroutineMeanHardDriveUsageMeasurement");
	public static final Resource UnstructuredDataToRDFSubroutineMeanHardDriveUsageMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#UnstructuredDataToRDFSubroutineMeanHardDriveUsageMeasurementBearer");
	public static final Resource Workflow = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#Workflow");
	public static final Resource WorkflowIdentifier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#WorkflowIdentifier");
	public static final Resource WorkflowIdentifierBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#WorkflowIdentifierBearer");

	// DatatypeProperty
	public static final Property hasAvailability = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasAvailability");
	public static final Property hasCPUCost = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasCPUCost");
	public static final Property hasInputArgumentName = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasInputArgumentName");
	public static final Property hasMemoryCost = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasMemoryCost");
	public static final Property hasModelNamespace = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasModelNamespace");
	public static final Property hasOutputArgumentName = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasOutputArgumentName");
	public static final Property hasTimeCost = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasTimeCost");
	public static final Property hasUtility = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasUtility");
	public static final Property has_mean = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#has_mean");
	public static final Property has_standard_deviation = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#has_standard_deviation");
	public static final Property is_decision_node = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#is_decision_node");
	public static final Property requiresNonNullValue = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#requiresNonNullValue");
	public static final Property requiresNullValue = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#requiresNullValue");
	public static final Property requiresValue = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#requiresValue");

	// ObjectProperty
	public static final Property hasAction = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasAction");
	public static final Property hasContainedWithin = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasContainedWithin");
	public static final Property hasContext = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasContext");
	public static final Property hasContextAlternative = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasContextAlternative");
	public static final Property hasContract = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasContract");
	public static final Property hasCriteria = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasCriteria");
	public static final Property hasDecision = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasDecision");
	public static final Property hasGlobalContext = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasGlobalContext");
	public static final Property hasIndividual = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasIndividual");
	public static final Property hasInequality = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasInequality");
	public static final Property hasInvariant = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasInvariant");
	public static final Property hasMapping = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasMapping");
	public static final Property hasMaximum = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasMaximum");
	public static final Property hasMinimum = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasMinimum");
	public static final Property hasOperation = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasOperation");
	public static final Property hasOrder = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasOrder");
	public static final Property hasPosition = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasPosition");
	public static final Property hasProperty = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasProperty");
	public static final Property hasResult = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasResult");
	public static final Property hasSourceContainer = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasSourceContainer");
	public static final Property hasStateChange = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasStateChange");
	public static final Property hasUniqueWithin = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasUniqueWithin");
	public static final Property hasValueType = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasValueType");
	public static final Property hasValueTypeAlternative = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasValueTypeAlternative");
	public static final Property hasVariable = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#hasVariable");
	public static final Property has_query_result = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#has_query_result");
	public static final Property is_designated_by_bearer = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#is_designated_by_bearer");
	public static final Property merges_into_root = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/AlgorithmicOntology#merges_into_root");
}

