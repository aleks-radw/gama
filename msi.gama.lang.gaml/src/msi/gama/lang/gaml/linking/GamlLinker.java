/**
 * Created by drogoul, 6 avr. 2012
 * 
 */
package msi.gama.lang.gaml.linking;

import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.IErrorCollector;
import msi.gama.kernel.model.IModel;
import msi.gama.runtime.GAMA;
import msi.gaml.compilation.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.diagnostics.IDiagnosticConsumer;
import org.eclipse.xtext.linking.lazy.LazyLinker;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;

/**
 * The class GamlLinker.
 * 
 * @author drogoul
 * @since 6 avr. 2012
 * 
 */
public class GamlLinker extends LazyLinker implements IGamlBuilder, IErrorCollector {

	private final Map<Resource, IGamlBuilder.Listener> listeners = new HashMap();

	public GamlLinker() {
		GAMA.setGamlBuilder(this);
	}

	// private final Map<Resource, GamlBuilder> builders = new HashMap();
	private boolean hasErrors;

	@Override
	protected void afterModelLinked(final EObject model, final IDiagnosticConsumer d) {
		hasErrors = false;
		if ( !GamaBundleLoader.contributionsLoaded ) { return; }
		Resource r = model.eResource();
		validate(r);
	}

	private GamlBuilder getBuilder(final Resource r) {
		// GamlBuilder result = builders.get(r);
		// if ( result == null ) {
		GamlBuilder result = new GamlBuilder((XtextResource) r, this);
		// builders.put(r, result);
		// }
		return result;
	}

	private void error(final String message, final EObject object) {
		hasErrors = true;
		if ( object == null ) { return; }
		object.eResource().getErrors()
			.add(new GamlDiagnostic("", new String[0], message, NodeModelUtils.getNode(object)));
	}

	private void warning(final String message, final EObject object) {
		if ( object == null ) { return; }
		object.eResource().getWarnings()
			.add(new GamlDiagnostic("", new String[0], message, NodeModelUtils.getNode(object)));
	}

	/**
	 * @see msi.gama.common.util.IErrorCollector#add(msi.gaml.compilation.GamlCompilationError)
	 */
	@Override
	public void add(final GamlCompilationError e) {
		if ( e.isWarning() ) {
			warning(e.toString(), (EObject) e.getStatement());
		} else {
			error(e.toString(), (EObject) e.getStatement());
		}
	}

	@Override
	public boolean hasErrors() {
		return hasErrors;
	}

	/**
	 * @param r
	 * @return
	 */
	public Map<Resource, ISyntacticElement> buildCompleteSyntacticTree(final Resource r) {
		Map<Resource, ISyntacticElement> trees = new LinkedHashMap();
		getBuilder(r).buildRecursiveSyntacticTree(trees, r);
		return trees;
	}

	/**
	 * @see msi.gama.common.interfaces.IGamlBuilder#build(org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	public IModel build(final Resource xtextResource) {
		try {
			return getBuilder(xtextResource).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void validate(final Resource xtextResource) {
		boolean isOK = false;
		try {
			isOK = getBuilder(xtextResource).validate();
		} catch (Exception e) {
			isOK = false;
			e.printStackTrace();
		}
		IGamlBuilder.Listener listener = listeners.get(xtextResource);
		if ( listener != null ) {
			listener.validationEnded(xtextResource);
		}
	}

	/**
	 * @see msi.gama.common.interfaces.IGamlBuilder#addListener(org.eclipse.emf.ecore.resource.Resource,
	 *      msi.gama.common.interfaces.IGamlBuilder.Listener)
	 */
	@Override
	public void addListener(final Resource xtextResource, final Listener listener) {
		listeners.put(xtextResource, listener);
	}

	/**
	 * @see msi.gama.common.interfaces.IGamlBuilder#removeListener(msi.gama.common.interfaces.IGamlBuilder.Listener)
	 */
	@Override
	public void removeListener(final Listener listener) {
		Resource toRemove = null;
		for ( Map.Entry<Resource, IGamlBuilder.Listener> entry : listeners.entrySet() ) {
			if ( entry.getValue().equals(listener) ) {
				toRemove = entry.getKey();
				break;
			}
		}
		if ( toRemove != null ) {
			listeners.remove(toRemove);
		}
	}

}
