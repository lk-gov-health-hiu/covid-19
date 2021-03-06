/*
 * The MIT License
 *
 * Copyright 2020 rush.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Imports">
import lk.gov.health.phsp.entity.Component;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Phn;
import lk.gov.health.phsp.facade.PhnFacade;
// </editor-fold>

/**
 *
 * @author rush
 */


@Named
@SessionScoped
public class PhnController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private PhnFacade ejbFacade;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Contollers">
    @Inject
    ApplicationController applicationController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public PhnController() {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Functions">
    
    public Phn getPhnById(java.lang.Long id) {
        return getFacade().find(id);
    }
    // </editor-fold>
    
    
    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public PhnFacade getFacade() {
        return ejbFacade;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Converter">

    @FacesConverter(forClass = Phn.class)
    public static class PhnControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PhnController controller = (PhnController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "phnController");
            return controller.getPhnById(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Phn) {
                Phn o = (Phn) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Component.class.getName()});
                return null;
            }
        }

    }
    
    // </editor-fold>


}

