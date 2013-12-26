/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class emo
{
	private static final String PREFIX = "emo";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource ActOfAssistance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#ActOfAssistance");
	public static final Resource ActOfHearing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#ActOfHearing");
	public static final Resource ActOfSmell = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#ActOfSmell");
	public static final Resource ActOfSocialBehavior = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#ActOfSocialBehavior");
	public static final Resource ActOfTaste = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#ActOfTaste");
	public static final Resource ActOfTouch = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#ActOfTouch");
	public static final Resource ActOfVision = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#ActOfVision");
	public static final Resource AestheticPleasure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#AestheticPleasure");
	public static final Resource Amusement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Amusement");
	public static final Resource AnatomicalStructure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#AnatomicalStructure");
	public static final Resource Anger = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Anger");
	public static final Resource AnimalNatureDisgust = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#AnimalNatureDisgust");
	public static final Resource Annoyance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Annoyance");
	public static final Resource Anxiety = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Anxiety");
	public static final Resource AppraisalProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#AppraisalProcess");
	public static final Resource Behavior = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Behavior");
	public static final Resource BehaviorInducingState = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#BehaviorInducingState");
	public static final Resource BodilyProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#BodilyProcess");
	public static final Resource BodilyQuality = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#BodilyQuality");
	public static final Resource CognitiveProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#CognitiveProcess");
	public static final Resource CognitiveRepresentation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#CognitiveRepresentation");
	public static final Resource CompanionateLove = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#CompanionateLove");
	public static final Resource Compassion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Compassion");
	public static final Resource Contempt = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Contempt");
	public static final Resource Contentment = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Contentment");
	public static final Resource CoreDisgust = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#CoreDisgust");
	public static final Resource Despair = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Despair");
	public static final Resource Disappointment = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Disappointment");
	public static final Resource Disgust = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Disgust");
	public static final Resource DispositionToBeAgentOfMentalProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#DispositionToBeAgentOfMentalProcess");
	public static final Resource Elation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Elation");
	public static final Resource Embarrassment = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Embarrassment");
	public static final Resource EmotionOccurrent = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#EmotionOccurrent");
	public static final Resource EmotionalBehaviouralProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#EmotionalBehaviouralProcess");
	public static final Resource Euphoria = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Euphoria");
	public static final Resource Fear = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Fear");
	public static final Resource Fury = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Fury");
	public static final Resource Grief = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Grief");
	public static final Resource Guilt = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Guilt");
	public static final Resource Happiness = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Happiness");
	public static final Resource Hate = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Hate");
	public static final Resource Interest = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Interest");
	public static final Resource InterpersonalDisgust = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#InterpersonalDisgust");
	public static final Resource Irritation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Irritation");
	public static final Resource Jealousy = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Jealousy");
	public static final Resource Joy = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Joy");
	public static final Resource Learning = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Learning");
	public static final Resource Love = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Love");
	public static final Resource MasteryPleasure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#MasteryPleasure");
	public static final Resource Memory = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Memory");
	public static final Resource MentalFunctioningRelatedAnatomicalStructure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#MentalFunctioningRelatedAnatomicalStructure");
	public static final Resource MentalProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#MentalProcess");
	public static final Resource MoralDisgust = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#MoralDisgust");
	public static final Resource Panic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Panic");
	public static final Resource PassionateLove = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#PassionateLove");
	public static final Resource Perception = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Perception");
	public static final Resource PhysiologicalResponseToEmotionProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#PhysiologicalResponseToEmotionProcess");
	public static final Resource Planning = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Planning");
	public static final Resource Pleasure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Pleasure");
	public static final Resource Pride = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Pride");
	public static final Resource Rage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Rage");
	public static final Resource Sadness = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Sadness");
	public static final Resource SensoryPerception = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#SensoryPerception");
	public static final Resource SensoryPleasure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#SensoryPleasure");
	public static final Resource Serenity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Serenity");
	public static final Resource SexualPleasure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#SexualPleasure");
	public static final Resource Shame = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Shame");
	public static final Resource SocialPleasure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#SocialPleasure");
	public static final Resource StressEmotion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#StressEmotion");
	public static final Resource Surprise = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Surprise");
	public static final Resource Terror = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Terror");
	public static final Resource Thinking = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Thinking");
	public static final Resource Wanting = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/AIRSEmotionOntology#Wanting");
}

