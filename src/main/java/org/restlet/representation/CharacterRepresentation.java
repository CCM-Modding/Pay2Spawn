/**
 * Copyright 2005-2013 Restlet S.A.S.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 *
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.representation;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.BioUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Representation based on a BIO character stream.
 *
 * @author Jerome Louvel
 */
public abstract class CharacterRepresentation extends Representation
{
    /**
     * Constructor.
     *
     * @param mediaType The media type.
     */
    public CharacterRepresentation(MediaType mediaType)
    {
        super(mediaType);
        setCharacterSet(CharacterSet.UTF_8);
    }

    @Override
    public java.nio.channels.ReadableByteChannel getChannel() throws IOException
    {
        return org.restlet.engine.io.NioUtils.getChannel(getStream());
    }

    @Override
    public InputStream getStream() throws IOException
    {
        return BioUtils.getStream(getReader(), getCharacterSet());
    }

    @Override
    public void write(OutputStream outputStream) throws IOException
    {
        Writer writer = BioUtils.getWriter(outputStream, getCharacterSet());
        write(writer);
        writer.flush();
    }

    @Override
    public void write(java.nio.channels.WritableByteChannel writableChannel) throws IOException
    {
        OutputStream os = org.restlet.engine.io.NioUtils.getStream(writableChannel);
        write(os);
        os.flush();
    }

}
