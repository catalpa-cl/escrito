package de.unidue.ltl.escrito.demo.gui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


public class MyUI extends UI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();

		Label label = new Label("Question?");
		layout.addComponent(label);

		TextField tf = new TextField("Your answer:");
		tf.setValue("Type your answer here.");
		layout.addComponent(tf);

		Button scoreButton = new Button("Score my answer!");
		scoreButton.addClickListener(e -> {

		});
		layout.addComponent(scoreButton);


	}



	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}
}

