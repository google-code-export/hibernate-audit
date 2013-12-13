package com.googlecode.hibernate.audit.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.match.eobject.EObjectIndex;
import org.eclipse.emf.compare.match.eobject.EObjectIndex.Side;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
import org.eclipse.emf.compare.match.eobject.ScopeQuery;
import org.eclipse.emf.compare.match.eobject.internal.ByTypeIndex;
import org.eclipse.emf.compare.match.eobject.internal.MatchAheadOfTime;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class FixedProximityEObjectMatcher implements IEObjectMatcher, ScopeQuery {
	/**
	 * Number of elements to index before a starting a match ahead step.
	 */
	private static final int NB_ELEMENTS_BETWEEN_MATCH_AHEAD = 10000;

	/**
	 * The index which keep the EObjects.
	 */
	private EObjectIndex index;

	/**
	 * Keeps track of which side was the EObject from.
	 */
	private Map<EObject, Side> eObjectsToSide = Maps.newHashMap();

	/**
	 * Create the matcher using the given distance function.
	 * 
	 * @param meter
	 *            a function to measure the distance between two {@link EObject}s.
	 */
	public FixedProximityEObjectMatcher(DistanceFunction meter) {
		this.index = new ByTypeIndex(meter, this);
	}

	/**
	 * {@inheritDoc}
	 */

	public void createMatches(Comparison comparison, Iterator<? extends EObject> leftEObjects,
			Iterator<? extends EObject> rightEObjects, Iterator<? extends EObject> originEObjects,
			Monitor monitor) {

		// FIXME: how to create an EMF submonitor
		Monitor subMonitor = new BasicMonitor();
		subMonitor.beginTask("indexing objects", 1);
		int nbElements = 0;
		int lastSegment = 0;
		/*
		 * We are iterating through the three sides of the scope at the same time so that index might apply
		 * pre-matching strategies elements if they wish.
		 */
		while (leftEObjects.hasNext() || rightEObjects.hasNext() || originEObjects.hasNext()) {

			if (leftEObjects.hasNext()) {
				EObject next = leftEObjects.next();
				nbElements++;
				index.index(next, Side.LEFT);
				eObjectsToSide.put(next, Side.LEFT);
			}

			if (rightEObjects.hasNext()) {
				EObject next = rightEObjects.next();
				index.index(next, Side.RIGHT);
				eObjectsToSide.put(next, Side.RIGHT);
			}

			if (originEObjects.hasNext()) {
				EObject next = originEObjects.next();
				index.index(next, Side.ORIGIN);
				eObjectsToSide.put(next, Side.ORIGIN);
			}
			if (nbElements / NB_ELEMENTS_BETWEEN_MATCH_AHEAD > lastSegment) {
				matchAheadOfTime(comparison, subMonitor);
				lastSegment++;
			}

		}

		subMonitor.worked(1);
		subMonitor.done();

		// FIXME: how to create an EMF submonitor
		subMonitor = new BasicMonitor();
		subMonitor.beginTask("matching objects", nbElements);

		matchIndexedObjects(comparison, subMonitor);

		createUnmatchesForRemainingObjects(comparison);
		subMonitor.done();
		restructureMatchModel(comparison);

	}

	/**
	 * If the index supports it, match element ahead of time, in case of failure the elements are kept and
	 * will be processed again later on.
	 * 
	 * @param comparison
	 *            the current comoparison.
	 * @param monitor
	 *            monitor to track progress.
	 */
	private void matchAheadOfTime(Comparison comparison, Monitor monitor) {
		if (index instanceof MatchAheadOfTime) {
			matchList(comparison, ((MatchAheadOfTime)index).getValuesToMatchAhead(Side.LEFT), false, monitor);
			matchList(comparison, ((MatchAheadOfTime)index).getValuesToMatchAhead(Side.RIGHT), false, monitor);
		}
	}

	/**
	 * Match elements for real, if no match is found for an element, an object will be created to represent
	 * this unmatch and the element will not be processed again.
	 * 
	 * @param comparison
	 *            the current comparison.
	 * @param subMonitor
	 *            monitor to track progress.
	 */
	private void matchIndexedObjects(Comparison comparison, Monitor subMonitor) {
		Iterable<EObject> todo = index.getValuesStillThere(Side.LEFT);
		while (todo.iterator().hasNext()) {
			todo = matchList(comparison, todo, true, subMonitor);
		}
		todo = index.getValuesStillThere(Side.RIGHT);
		while (todo.iterator().hasNext()) {
			todo = matchList(comparison, todo, true, subMonitor);
		}

	}

	/**
	 * Create all the Match objects for the remaining EObjects.
	 * 
	 * @param comparison
	 *            the current comparison.
	 */
	private void createUnmatchesForRemainingObjects(Comparison comparison) {
		for (EObject notFound : index.getValuesStillThere(Side.RIGHT)) {
			areMatching(comparison, null, notFound, null);
		}
		for (EObject notFound : index.getValuesStillThere(Side.LEFT)) {
			areMatching(comparison, notFound, null, null);
		}
		for (EObject notFound : index.getValuesStillThere(Side.ORIGIN)) {
			areMatching(comparison, null, null, notFound);
		}
	}

	/**
	 * Process the list of objects matching them. This method might not be able to process all the EObjects if
	 * - for instance, their container has not been matched already. Every object which could not be matched
	 * is returned in the list.
	 * 
	 * @param comparison
	 *            the comparison being built.
	 * @param todoList
	 *            the list of objects to process.
	 * @param createUnmatches
	 *            whether elements which have no match should trigger the creation of a Match object (meaning
	 *            we won't try to match them afterwards) or not.
	 * @param monitor
	 *            a monitor to track progress.
	 * @return the list of EObjects which could not be processed for some reason.
	 */
	private Iterable<EObject> matchList(Comparison comparison, Iterable<EObject> todoList,
			boolean createUnmatches, Monitor monitor) {
		Set<EObject> remainingResult = Sets.newLinkedHashSet();
		List<EObject> requiredContainers = Lists.newArrayList();
		Iterator<EObject> todo = todoList.iterator();
		while (todo.hasNext()) {
			EObject next = todo.next();
			/*
			 * Let's first add every container which is in scope
			 */
			EObject container = next.eContainer();
			while (container != null && isInScope(container)) {
				if (comparison.getMatch(container) == null) {
					requiredContainers.add(0, container);
				}
				container = container.eContainer();
			}
		}
		Iterator<EObject> containersAndTodo = Iterators.concat(requiredContainers.iterator(), todoList
				.iterator());
		while (containersAndTodo.hasNext()) {
			EObject next = containersAndTodo.next();
			/*
			 * At this point you need to be sure the element has not been matched in any other way before.
			 */
			if (comparison.getMatch(next) == null) {
				if (!tryToMatch(comparison, next, createUnmatches)) {
					remainingResult.add(next);
					monitor.worked(1);
				}
			}
		}
		return remainingResult;
	}

	/**
	 * Try to create a Match. If the match got created, register it (having actual left/right/origin matches
	 * or not), if not, then return false. Cases where it might not create the match : if some required data
	 * has not been computed yet (for instance if the container of an object has not been matched and if the
	 * distance need to know if it's match to find the children matches).
	 * 
	 * @param comparison
	 *            the comparison under construction, it will be updated with the new match.
	 * @param a
	 *            object to match.
	 * @param createUnmatches
	 *            whether elements which have no match should trigger the creation of a Match object (meaning
	 *            we won't try to match them afterwards) or not.
	 * @return false if the conditions are not fulfilled to create the match, true otherwhise.
	 */
	private boolean tryToMatch(Comparison comparison, EObject a, boolean createUnmatches) {
		boolean okToMatch = false;
		Side aSide = eObjectsToSide.get(a);
		assert aSide != null;
		Side bSide = Side.LEFT;
		Side cSide = Side.RIGHT;
		if (aSide == Side.RIGHT) {
			bSide = Side.LEFT;
			cSide = Side.ORIGIN;
		} else if (aSide == Side.LEFT) {
			bSide = Side.RIGHT;
			cSide = Side.ORIGIN;
		} else if (aSide == Side.ORIGIN) {
			bSide = Side.LEFT;
			cSide = Side.RIGHT;
		}
		assert aSide != bSide;
		assert bSide != cSide;
		assert cSide != aSide;
		Map<Side, EObject> closests = index.findClosests(comparison, a, aSide);
		if (closests != null) {
			EObject lObj = closests.get(bSide);
			EObject aObj = closests.get(cSide);
			if (lObj != null || aObj != null) {
				// we have at least one other match
				areMatching(comparison, closests.get(Side.LEFT), closests.get(Side.RIGHT), closests
						.get(Side.ORIGIN));
				okToMatch = true;
			} else if (createUnmatches) {
				areMatching(comparison, closests.get(Side.LEFT), closests.get(Side.RIGHT), closests
						.get(Side.ORIGIN));
				okToMatch = true;
			}
		}
		return okToMatch;
	}

	/**
	 * Process all the matches of the given comparison and re-attach them to their parent if one is found.
	 * 
	 * @param comparison
	 *            the comparison to restructure.
	 */
	private void restructureMatchModel(Comparison comparison) {
		Iterator<Match> it = ImmutableList.copyOf(Iterators.filter(comparison.eAllContents(), Match.class))
				.iterator();

		while (it.hasNext()) {
			Match cur = it.next();
			EObject possibleContainer = null;
			if (cur.getLeft() != null) {
				possibleContainer = cur.getLeft().eContainer();
			}
			if (possibleContainer == null && cur.getRight() != null) {
				possibleContainer = cur.getRight().eContainer();
			}
			if (possibleContainer == null && cur.getOrigin() != null) {
				possibleContainer = cur.getOrigin().eContainer();
			}
			Match possibleContainerMatch = comparison.getMatch(possibleContainer);
			if (possibleContainerMatch != null) {
				((BasicEList<Match>)possibleContainerMatch.getSubmatches()).addUnique(cur);
			}
		}
	}

	/**
	 * Register the given object as a match and add it in the comparison.
	 * 
	 * @param comparison
	 *            container for the Match.
	 * @param left
	 *            left element.
	 * @param right
	 *            right element
	 * @param origin
	 *            origin element.
	 * @return the created match.
	 */
	private Match areMatching(Comparison comparison, EObject left, EObject right, EObject origin) {
		Match result = CompareFactory.eINSTANCE.createMatch();
		result.setLeft(left);
		result.setRight(right);
		result.setOrigin(origin);
		((BasicEList<Match>)comparison.getMatches()).addUnique(result);
		if (left != null) {
			index.remove(left, Side.LEFT);
		}
		if (right != null) {
			index.remove(right, Side.RIGHT);
		}
		if (origin != null) {
			index.remove(origin, Side.ORIGIN);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInScope(EObject eContainer) {
		return eObjectsToSide.get(eContainer) != null;
	}
}